package com.jcca.web.asset.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.exception.ResultException;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.web.asset.controller.bean.ChangeProcessModeReq;
import com.jcca.web.asset.dao.ThresholdProcessMapper;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdProcessService;
import com.jcca.web.asset.utils.enums.ThresholdProcessStatusEnum;
import com.jcca.web.collect.service.CollectProcessService;
import com.jcca.web.common.constants.BizManageConstant;
import com.jcca.web.common.service.BizManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 线程阈值
 *
 * @author Lvyp
 */
@Service
@Slf4j
public class ThresholdProcessServiceImpl extends ServiceImpl<ThresholdProcessMapper, ThresholdProcess>
        implements ThresholdProcessService {

    @Resource
    private BizManageService bizService;
    @Resource
    private ThresholdProcessMapper thresholdProcessMapper;
    @Resource
    private RedisService redisServ;
    @Resource
    private CollectProcessService processServ;
    @Resource
    private AssetService assetServ;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAll(List<ThresholdProcess> entityList) {
        for (ThresholdProcess entity : entityList) {
            String assetId = entity.getAssetId();
            Asset asset = assetServ.getById(assetId);
            if (Objects.isNull(asset)) {
                throw new ResultException(ResultEnum.ASSET_NULL);
            }
            if (StrUtil.isEmpty(asset.getOrgId())) {
                throw new ResultException(ResultEnum.ORG_NULL);
            }

            bizService.saveByBizAndOrg(asset.getOrgId(), entity.getId(), BizManageConstant.THRESHOLD_PROCESS);
        }
        this.saveBatch(entityList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(ThresholdProcess process) {
        bizService.remove(process.getId(), BizManageConstant.THRESHOLD_PROCESS);
        thresholdProcessMapper.deleteById(process.getId());
        //清空采集数据
        processServ.removeByAssetIdAndProcessName(process.getAssetId(), process.getProcessName());
    }

    @Override
    public ThresholdProcess getOneByAssetIpAndName(String assetIp, String processName) {
        Asset asset = assetServ.findOneByIp(assetIp);
        QueryWrapper<ThresholdProcess> queryWrapper = new QueryWrapper<ThresholdProcess>();
        queryWrapper.eq("ASSET_ID", asset.getId());
        List<ThresholdProcess> thresholdProcesses = thresholdProcessMapper.selectList(queryWrapper);
        for (ThresholdProcess thresholdProcess : thresholdProcesses) {
            boolean contains = processName.contains(thresholdProcess.getProcessName());
            if(contains){
                return thresholdProcess;
            }
        }
        return null;
    }

    /**
     * 改变进程的配置模式(双机双活,双击单活,普通)
     *
     * @param req              (id)      : 进程数据的ID
     * @param req              (mode)   : 模式:1双机单活,2双机双活,3普通
     * @param req(processName) : 进程名称
     * @return: com.jcca.common.vo.ResultVo<?>
     * @Author: syt
     * @Date: 2021/8/10/010 16:40
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<?> modeChange(ChangeProcessModeReq req) {
        if (StrUtil.isEmpty(req.getId()) || req.getMode() == null) {
            throw new ResultException(ResultEnum.ERROR.getCode(), "参数不可为空!请检查");
        }

        if (LogInputUtils.inputInfo(ServerTypeEnum.PROCESS_MANAGER)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.PROCESS_MANAGER, "进程：" + req.getProcessName(), String.format("请求更改进程id为 %s, 更改为模式代码为: %s。id参数类型为：【 %s 】，模式代码类型为：【 %s 】",
                    req.getId(), req.getMode(), req.getId().getClass().getTypeName(), req.getMode().getClass().getTypeName())));
        }

        // 查询进程信息
        List<ThresholdProcess> processList = thresholdProcessMapper.selectList(new QueryWrapper<ThresholdProcess>().eq("ID", req.getId()));
        if (Objects.isNull(processList) && !processList.isEmpty()) {
            throw new ResultException(ResultEnum.ERROR.getCode(), "无该进程信息,无法更改");
        }
        ThresholdProcess process = processList.get(0);

        //模式没有改变  无需修改
        if (req.getMode() == process.getHostMode()) {
            return ResultVoUtil.success();
        }

        // 根据assetCode查询是否有同组设备
        Asset asset = assetServ.getById(process.getAssetId());
        QueryWrapper<Asset> queryAsset = Wrappers.query();
        queryAsset.eq("ASSET_CODE", asset.getAssetCode());
        queryAsset.eq("IS_DEL", "1");
        List<Asset> assetGroupList = assetServ.list(queryAsset);
        List<String> assetIds = assetGroupList.stream().map(Asset::getId).collect(Collectors.toList());

        if (req.getMode() == 1 || req.getMode() == 2) {
            if (assetGroupList.size() == 1) {
                throw new ResultException(ResultEnum.ERROR.getCode(), "该进程所属设备无组配置,更改进程模式失败");

            } else if (assetGroupList.size() == 2) {

                QueryWrapper<ThresholdProcess> queryProcess = Wrappers.query();
                queryProcess.in("ASSET_ID", assetIds);
                queryProcess.eq("PROCESS_NAME", req.getProcessName());
                List<ThresholdProcess> thresholdProcesses = thresholdProcessMapper.selectList(queryProcess);

                if (thresholdProcesses.size() < 2) {// 查看同组设备是否有同名进程
                    throw new ResultException(ResultEnum.ERROR.getCode(), "无法获取对端设备的相同进程信息");
                } else if (thresholdProcesses.size() == 2) {
                    for (String assetId : assetIds) {
                        thresholdProcessMapper.updateGroupHostMode(assetId, process.getProcessName(), req.getMode());
                    }
                    return ResultVoUtil.success("设置成功");
                } else {
                    throw new ResultException(ResultEnum.ERROR.getCode(), "组设备内进程个数大于2");
                }

            } else {
                throw new ResultException(ResultEnum.ERROR.getCode(), "进程组最多配置2台资产");
            }

        } else if (req.getMode() == 3) {

            for (String assetId : assetIds) {
                thresholdProcessMapper.updateGroupHostMode(assetId, process.getProcessName(), req.getMode());
            }

        } else {
            throw new ResultException(ResultEnum.ERROR.getCode(), "未知模式设置,请检查传入参数");
        }

        return ResultVoUtil.success("设置成功");


    }

    /***
     * 分别更新单个进程的状态,用于进程管理中的显示 syt
     *
     * @param processName : 进程名称
     * @param id : 资产的ID
     * @return: void
     * @Author: syt
     * @Date: 2021/8/24/024 10:26
     */
    @Override
    public void updateByAssetIdAndProcessName(String processName, String id, Boolean collectStatus, String processId) {
        Integer thresholdCollectStatus;
        if (collectStatus) {
            thresholdCollectStatus = ThresholdProcessStatusEnum.NORMAL.getCode();
        } else {
            thresholdCollectStatus = ThresholdProcessStatusEnum.ABNORMAL.getCode();
        }
        thresholdProcessMapper.updateByAssetIdAndProcessName(processName, id, thresholdCollectStatus, processId);
    }

    /**
     * 根据资产ID删除进程
     *
     * @param assetId : 资产ID
     * @return: void
     * @Author: syt
     * @Date: 2021/8/30/030 13:41
     */
    @Override
    public void removeByAssetId(String assetId) {
        if (StrUtil.isEmpty(assetId)) {
            log.error("资产ID为空,无法匹配删除进程!");
            return;
        }
        // 查进程名,用于清除redis缓存
        QueryWrapper<ThresholdProcess> query = Wrappers.query();
        query.eq("ASSET_ID", assetId);
        List<ThresholdProcess> processList = thresholdProcessMapper.selectList(query);
        // 查assetCode
        String assetCode = assetServ.getById(assetId).getAssetCode();
        for (ThresholdProcess tp : processList) {
            // 清除redis缓存信息
            redisServ.remove("GROUP:PROCESS:" + assetCode + tp.getProcessName());
        }
        //删除进程
        QueryWrapper<ThresholdProcess> wrapper = Wrappers.query();
        wrapper.eq("ASSET_ID", assetId);
        thresholdProcessMapper.delete(wrapper);
    }

    /**
     * 更新采集模式
     */
    @Override
    public void updateMode(String assetCode, Integer hostMode) {
        if (ObjectUtil.isNull(assetCode)) {
            assetCode = "";
        }
        thresholdProcessMapper.updateHostModeByAssetCode(assetCode, hostMode);
    }

    @Override
    public List<ThresholdProcess> selectByAssetList(List<String> assetList) {
        QueryWrapper<ThresholdProcess> query = new QueryWrapper<ThresholdProcess>();
        query.in("ASSET_ID",assetList);

        return thresholdProcessMapper.selectList(query);
    }


}
