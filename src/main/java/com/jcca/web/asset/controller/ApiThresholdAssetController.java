package com.jcca.web.asset.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.RedisCacheConst;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.bean.constant.ThresholdAutoFlagConst;
import com.jcca.common.log.annotation.ActionLog;
import com.jcca.common.log.constant.LogTypeConstant;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.dataProcessing.manager.threshold.Event;
import com.jcca.dataProcessing.support.ListenerManager;
import com.jcca.web.asset.controller.bean.ThresholdSectionResp;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.ThresholdAsset;
import com.jcca.web.asset.entity.ThresholdSection;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.service.ThresholdSectionService;
import com.jcca.web.asset.vo.ThresholdAssetReq;
import com.jcca.web.asset.vo.ThresholdAssetVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * @ClassName ApiThresholdAssetController
 * @Description 资产阈值管理
 * @Date 2020/5/19 10:22
 * @Author hanwone
 */
@Slf4j
@RestController
@RequestMapping("/api/threshold")
@Api(tags = "资产阈值管理")
public class ApiThresholdAssetController extends ListenerManager {

    @Resource
    private ThresholdAssetService thresholdAssetService;
    @Resource
    private AssetService assetservice;
    @Resource
    private RedisService redisService;
    @Resource
    private ThresholdSectionService thresholdSectionService;





    @PostMapping("/removeSpecial")
    @ApiOperation(value = "删除手动阈值")
    @RequiresPermissions("api:threshold:one")
    @ActionLog(name = "删除手动阈值", title = "监控管理", key = LogTypeConstant.REMOVEE)
    public ResultVo<?> initSection(String assetId) {

        List<String> assetIds = ShiroUtil.getSubjectAssetIds();
        thresholdAssetService.removeSpecial(assetId, assetIds);
        redisService.remove(RedisCacheConst.THRESHOLD_ALARM_PRE + assetId);
        this.dispatureEvent(new Event());

        //
        try {
            thresholdAssetService.recoverAlarm(assetId);
        } catch (Exception e) {
            log.error("资产阈值管理-删除手动阈值异常：{}", e.getMessage(), e);
            return ResultVoUtil.error("处理阈值告警异常");
        }

        return ResultVoUtil.success();
    }

    /**
     * 区间阈值
     * 查询设置区间阈值
     *
     * @return
     */
    @GetMapping("/initSection")
    @ApiOperation(value = "查询设备设置的区间阈值")
    @RequiresPermissions("api:threshold:section")
    public ResultVo<?> initSection(String assetId, String type, String flag) {
        if (StrUtil.isEmpty(assetId) || StrUtil.isEmpty(type)) {
            return ResultVoUtil.error("缺少查询参数");
        }

        ThresholdSection selectOne = thresholdSectionService.selectSectionConf(assetId, type, flag);
        redisService.remove(RedisCacheConst.THRESHOLD_ALARM_PRE + assetId);

        if (Objects.isNull(selectOne)) {
            return ResultVoUtil.success(selectOne);
        }

        ThresholdSectionResp resp = EntityBeanUtil.copy(selectOne, ThresholdSectionResp.class);
        resp.setSectionId(selectOne.getSectionId().toString());

        return ResultVoUtil.success(resp);
    }

    /**
     * 开启或者关闭区间阈值
     * 也可以调整最大最小值
     *
     * @return
     */
    @PostMapping("/upOrDownConf")
    @ApiOperation(value = "查询设备设置的区间阈值")
    @RequiresPermissions("api:threshold:section")
    public ResultVo<?> upOrDownConf(@RequestBody @Validated ThresholdSection req) {
        Date createDate = new Date();
        try {
            String lockStr = "api:threshold:section";
            synchronized (lockStr.intern()) {
                ThresholdSection selectOne = thresholdSectionService.selectSectionConf(req.getAssetId(), req.getType(), req.getFlag());
                if (Objects.isNull(selectOne)) {
                    req.setSectionId(Long.valueOf(MyIdUtil.getId()));
                    req.setCreateDate(createDate);
                    req.setUpdateDate(createDate);

                    thresholdSectionService.save(req);

                    return ResultVoUtil.success("处理成功");
                }

                req.setSectionId(null);
                ThresholdSection reqJson = EntityBeanUtil.replaceParameter(req, selectOne, ThresholdSection.class);

                reqJson.setUpdateDate(createDate);
                thresholdSectionService.updateById(reqJson);
                redisService.remove(RedisCacheConst.THRESHOLD_ALARM_PRE + req.getAssetId());
            }

            if (req.getStatus() == -1) {
                try {
                    thresholdAssetService.recoverAlarm(req.getAssetId());
                } catch (Exception e) {
                    log.error("资产阈值管理-查询资产区间阈值异常：{}", e.getMessage(), e);
                    return ResultVoUtil.success("设置完成但处理阈值告警异常");
                }
            }

            return ResultVoUtil.success("处理成功");
        } finally {
            this.dispatureEvent(new Event());
        }
    }

    /**
     * 按资产类型设置阈值默认值
     *
     * @return
     */
    @PostMapping("/default")
    @ApiOperation(value = "一键设置阈值默认值")
    @RequiresPermissions("api:threshold:default")
    @ActionLog(name = "按资产类型设置资产默认阈值", title = "监控管理", key = LogTypeConstant.MODIFY)
    public ResultVo setDefault(@Validated @RequestBody ThresholdAssetVo vo) {
        try {
            validateTimeDeviation(vo.getRunningTimeDeviationLinux(), "Linux运行时长");
            validateTimeDeviation(vo.getTimeDeviation(), "时间偏差");
            validateTimeDeviation(vo.getRunningTimeDeviationWindows(), "windows运行时长");
        }catch (Exception e){
            return ResultVoUtil.error(e.getMessage());
        }

        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        QueryWrapper<Asset> assetQuery = Wrappers.query();
        assetQuery.in("org_id", orgIds);
        assetQuery.eq("asset_mode", vo.getAssetMode());
        assetQuery.eq("is_del", StatusConst.OK);
        List<Asset> assetList = assetservice.list(assetQuery);

        List<ThresholdAsset> thresholdAssetList = new ArrayList<>();
        for (Asset asset : assetList) {

            if (Objects.nonNull(thresholdAssetService.querySpecialConf(asset.getId()))) {
                continue;
            }

            ThresholdAsset threshold = new ThresholdAsset();
            BeanUtil.copyProperties(vo, threshold);

            if (Objects.nonNull(asset.getCollectionType())) {
                if (Objects.nonNull(vo.getRunningTimeDeviationLinux()) && (asset.getCollectionType() == 0 || asset.getCollectionType() == 3)) {
                    threshold.setRunningTimeDeviation(vo.getRunningTimeDeviationLinux());
                } else if (Objects.nonNull(vo.getRunningTimeDeviationAix()) && (asset.getCollectionType() == 2 || asset.getCollectionType() == 4)) {
                    threshold.setRunningTimeDeviation(vo.getRunningTimeDeviationAix());
                } else if (Objects.nonNull(vo.getRunningTimeDeviationWindows()) && asset.getCollectionType() == 1) {
                    threshold.setRunningTimeDeviation(vo.getRunningTimeDeviationWindows());
                }
            }

            threshold.setAssetId(asset.getId());
            threshold.setAutoFlag(ThresholdAutoFlagConst.ORG_THRESHOLD);
            thresholdAssetList.add(threshold);
        }

        if (CollUtil.isNotEmpty(thresholdAssetList)) {
            thresholdAssetService.saveOrUpdateBatch(thresholdAssetList);
        }
        for (ThresholdAsset thresholdAsset : thresholdAssetList) {
            redisService.remove(RedisCacheConst.THRESHOLD_ALARM_PRE + thresholdAsset.getAssetId());
            try {
                thresholdAssetService.recoverAlarm(thresholdAsset.getAssetId());
            } catch (Exception e) {
                log.error("资产阈值管理-设置默认阈值失败：{}", e.getMessage(), e);
                return ResultVoUtil.success("设置完成但处理阈值告警异常");
            }
        }

        this.dispatureEvent(new Event());
        return ResultVoUtil.SAVE_SUCCESS;
    }


    private void validateTimeDeviation(Integer value, String field) throws Exception {
        if (value != null && value.intValue() > 99999) {
            throw new Exception(field + "只能输入五位以下整数");
        }
    }

    /**
     * 按资产类型查看阈值
     *
     * @return
     */
    @PostMapping("/look/default/{assetMode}")
    @ApiOperation(value = "查看默认阈值")
    @RequiresPermissions("api:threshold:look:default")
    public ResultVo viewDefault(@PathVariable("assetMode") Integer assetMode) {
        List<String> assetIds = ShiroUtil.getSubjectAssetIds();
        ThresholdAssetVo vo = thresholdAssetService.queryDefaultConf(assetIds, assetMode);
        return ResultVoUtil.success(vo);
    }

    /**
     * 查看单个资产阈值
     *
     * @return
     */
    @PostMapping("/look/one/{assetId}")
    @ApiOperation(value = "查看单个阈值")
    @RequiresPermissions("api:threshold:look:one")
    public ResultVo viewOne(@PathVariable("assetId") String assetId) {
        ThresholdAsset threshold = thresholdAssetService.getById(assetId);
        if (Objects.isNull(threshold)) {
            return ResultVoUtil.success("请先设置资产阈值");
        }
        ThresholdAssetVo vo = BeanUtil.copyProperties(threshold, ThresholdAssetVo.class);

        Asset asset = assetservice.getById(assetId);
        if (Objects.isNull(threshold.getRunningTimeDeviation())) {
            return ResultVoUtil.success(vo);
        }

        if (Arrays.asList(0, 3).contains(asset.getCollectionType())) {
            vo.setRunningTimeDeviationLinux(threshold.getRunningTimeDeviation());
        }
        if (Arrays.asList(2, 4).contains(asset.getCollectionType())) {
            vo.setRunningTimeDeviationAix(threshold.getRunningTimeDeviation());
        }
        if (Arrays.asList(1).contains(asset.getCollectionType())) {
            vo.setRunningTimeDeviationWindows(threshold.getRunningTimeDeviation());
        }

        return ResultVoUtil.success(vo);
    }

    /**
     * 设置单项阈值
     *
     * @return
     */
    @PostMapping("/one")
    @ApiOperation(value = "设置单项阈值")
    @RequiresPermissions("api:threshold:one")
    @ActionLog(name = "设置单个资产阈值", title = "监控管理", key = LogTypeConstant.MODIFY)
    public ResultVo setOne(@Validated @RequestBody ThresholdAssetReq threshold) {
        try {
            validateTimeDeviation(threshold.getTimeDeviation(), "时间偏差");
            validateTimeDeviation(threshold.getRunningTimeDeviationLinux(), "Linux运行时长");
            validateTimeDeviation(threshold.getRunningTimeDeviationWindows(), "windows运行时长");
        }catch (Exception e){
            return ResultVoUtil.error(e.getMessage());
        }

        String assetId = threshold.getAssetId();
        QueryWrapper<ThresholdAsset> query = Wrappers.query();
        query.eq("asset_id", assetId);
        ThresholdAsset one = thresholdAssetService.getOne(query);
        if (Objects.isNull(one)) {
            one = new ThresholdAsset();
        }

        Asset asset = assetservice.getById(assetId);

        BeanUtil.copyProperties(threshold, one);

        if (Objects.nonNull(asset.getCollectionType())) {
            if (Objects.nonNull(threshold.getRunningTimeDeviationLinux()) && asset.getCollectionType() == 0) {
                one.setRunningTimeDeviation(threshold.getRunningTimeDeviationLinux());
            } else if (Objects.nonNull(threshold.getRunningTimeDeviationWindows()) && asset.getCollectionType() == 1) {
                one.setRunningTimeDeviation(threshold.getRunningTimeDeviationWindows());
            } else if (Objects.nonNull(threshold.getRunningTimeDeviationAix()) && asset.getCollectionType() == 2) {
                one.setRunningTimeDeviation(threshold.getRunningTimeDeviationAix());
            }
        }

        one.setAutoFlag(ThresholdAutoFlagConst.MODE_THRESHOLD);
        thresholdAssetService.saveOrUpdate(one);

        redisService.remove(RedisCacheConst.THRESHOLD_ALARM_PRE + assetId);

        this.dispatureEvent(new Event());

        try {
            thresholdAssetService.recoverAlarm(assetId);
        } catch (Exception e) {
            log.error("资产阈值管理-设置单个阈值:{}", e.getMessage(), e);
            return ResultVoUtil.success("设置完成但处理阈值告警异常");
        }

        return ResultVoUtil.SAVE_SUCCESS;
    }

}
