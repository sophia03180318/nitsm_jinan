package com.jcca.web.asset.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.admin.system.service.TopoAssetPortService;
import com.jcca.admin.system.vo.AssetPortVo;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.bean.constant.ThresholdAutoFlagConst;
import com.jcca.common.enums.*;
import com.jcca.common.exception.ResultException;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.*;
import com.jcca.component.client.CollectAgent;
import com.jcca.component.client.enums.RealTimePingStatusEnum;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.controller.ApiAssetController;
import com.jcca.web.asset.controller.bean.AssetCollectReq;
import com.jcca.web.asset.controller.bean.AssetNetReq;
import com.jcca.web.asset.dao.AssetMapper;
import com.jcca.web.asset.dao.CabinetMapper;
import com.jcca.web.asset.dao.RoomMapper;
import com.jcca.web.asset.detail.bean.DetailProcess;
import com.jcca.web.asset.entity.*;
import com.jcca.web.asset.service.*;
import com.jcca.web.asset.service.bean.AddAssetException;
import com.jcca.web.asset.service.bean.AssetTelnetException;
import com.jcca.web.asset.utils.bean.CabinetUsed;
import com.jcca.web.asset.utils.enums.*;
import com.jcca.web.asset.vo.*;
import com.jcca.web.broken.service.BrokenRecordService;
import com.jcca.web.collect.entity.*;
import com.jcca.web.collect.enums.CollectNetCardStatus;
import com.jcca.web.collect.enums.SensorTypeEnum;
import com.jcca.web.collect.service.*;
import com.jcca.web.collect.service.bean.AssetDiskVo;
import com.jcca.web.common.constants.OutConst;
import com.jcca.web.common.service.OutService;
import com.jcca.web.common.vo.AssetCollectTestVo;
import com.jcca.web.common.vo.AssetTestResult;
import com.jcca.web.config.vo.SysConfig;
import com.jcca.web.graph.entity.TopoVertex;
import com.jcca.web.graph.service.TopoVertexService;
import com.jcca.web.ip.entity.IpInfo;
import com.jcca.web.ip.enums.IpPingStatusEnum;
import com.jcca.web.ip.service.IpInfoService;
import com.jcca.web.statistics.vo.StatisticsAlarmVo;
import com.jcca.web2.entity.*;
import com.jcca.web2.enums.AssetMonitorEnum;
import com.jcca.web2.service.*;
import com.jcca.web2.service.notify.NotifyDelAssetImpl;
import com.jcca.web2.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.telnet.TelnetClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import sun.net.util.IPAddressUtil;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author hanwone
 * @date 2020-04-20 15:37
 **/
@Service
@Slf4j
public class AssetServiceImpl extends ServiceImpl<AssetMapper, Asset> implements AssetService {

    @Resource
    private AssetMapper assetMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private SysOrgService orgService;
    @Resource
    private CabinetMapper cabinetMapper;
    @Resource
    private AssetAttachService assetAttachService;
    @Resource
    private IpInfoService ipInfoService;
    @Resource
    private OutService outService;
    @Resource
    private ThresholdAssetService thresholdAssetService;
    @Resource
    private TopoVertexService topoVertexServ;
    @Resource
    private BrokenRecordService brokenRecordServ;
    @Resource
    private AssetHardwareFixService fixServ;
    @Resource
    private ThresholdProcessService thresholdProcessService;
    @Resource
    private CollectRouteService routeServ;
    @Resource
    private CollectAgent collectAgent;
    @Resource
    private CollectCpuService cpuService;
    @Resource
    private CollectDiskService diskService;
    @Resource
    private CollectNetworkCardService netCardServ;
    @Resource
    private CollectMemoryService memoryServ;
    @Resource
    private CollectInterfacesService interfacesServ;
    @Autowired
    private List<AssetNotifyService> notifyServiceList;
    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private AssetModeService assetModeService;
    @Resource
    private CacheDataService cacheDataServ;
    @Resource
    private CollectSensorService sensorServ;
    @Resource
    private TopoAssetPortService topoAssetPortService;
    @Resource
    private CollectRaidService raidService;
    @Resource
    private CollectDsService dsService;
    @Resource
    private SysModuleConfigService sysModuleConfigService;
    @Resource
    private AssetManufacturerService assetManufacturerService;
    @Resource
    private AssetAppServerService appServerService;

    @Resource
    private CollectBhmTempInfoService collectBhmTempInfoService;
    @Resource
    private CollectBhmFanInfoService collectBhmFanInfoService;
    @Resource
    private CollectBhmPowerInfoService collectBhmPowerInfoService;



    @Value("${project.upload.static-url}")
    private String staticUrl;

    /**
     * 厂商设备数量统计
     *
     * @return 统计信息
     */
    @Override
    public List<StatisticsAlarmVo> getManufacturerAsset() {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }
        return assetMapper.getManufacturerAsset(orgIds);
    }

    /**
     * 保存导入的设备 仅限测试时使用！！！
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importSave(Asset asset) throws Exception {
        Asset selectByIp = assetMapper.selectByIp(asset.getIp());
        if (Objects.nonNull(selectByIp)) {
            if (LogInputUtils.inputError(ServerTypeEnum.ASSET)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.ASSET, ErrorCodeEnum.COMMON_VERIFY_REPETITION, asset.getIp(), "保存导入设备时发现已经存在IP为：" + asset.getIp() + "的设备！"));
            }
            return;
        }
        asset.setId(MyIdUtil.getId());
        ipInfoService.allocationIp(asset.getIp());
        assetMapper.insert(asset);
        // 设置默认阈值
        this.setDefaultThreshold(asset);
        // 更新保存资产附属信息表
        this.saveOrUpdateAttach(asset);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createAsset(Asset addReq) throws AddAssetException {
        String ip = addReq.getIp();
        boolean ipIsEmpty = StrUtil.isEmpty(ip);
        if (ipIsEmpty) {
            ip = "--";
        }
        //是否是模板导入
        boolean isTemplate = ObjectUtil.isNotNull(addReq.isTemplate()) && addReq.isTemplate();

        if (!isTemplate) {
            if (LogInputUtils.inputInfo(ServerTypeEnum.ASSET)) {
                log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.ASSET, addReq.getIp(), "资产保存原始数据:" + JSONUtil.toJsonStr(addReq)));
            }
            // 监控管理中添加资产时,如果之前没有校验则进行校验
            if (addReq.getBeforeVerify().equals("no")) {
                if (StatusConst.OK == addReq.getWatch() && ipIsEmpty) {
                    throw new AddAssetException(AddAssetException.VERIFY_ERROR, "监控设备必须录入IP", null);
                }
                // 检测IP网段是否配置
/*                if (!ipIsEmpty) {
                    Boolean examineIp = ipInfoService.examineIp(ip);
                    if (!examineIp) {
                        throw new AddAssetException(AddAssetException.VERIFY_ERROR, "请先配置IP网段", null);
                    }
                }*/
            }
        }
        // 检查IP是否已分配
        String key = "ASSET_VER_IP_KEY" + ip;
        synchronized (key.intern()) {
            // 监控管理中添加资产时,如果之前没有校验则进行校验
            if (!isTemplate && !ipIsEmpty && addReq.getBeforeVerify().equals("no")) {
                Asset one = this.findOneByIp(ip);
                if (Objects.isNull(one) && StrUtil.isNotEmpty(addReq.getIp2())) {
                    one = this.findOneByIp(ip);
                }
                if (Objects.nonNull(one)) {
                    String orgName = getAssetOrgName(one);
                    String msg = String.format("IP[%s]已分配给[%s]的设备[%s]", ip, orgName, one.getName());
                    throw new AddAssetException(AddAssetException.VERIFY_ERROR, msg, null);
                }
            }

            assetMapper.insert(addReq);
            saveOrUpdateAttach(addReq);

            // 监控管理中添加资产时,如果之前没有校验则进行校验
            if (addReq.getBeforeVerify().equals("no")) {
                // 验证指标
                if (StatusConst.OK != addReq.getWatch()) {
                    return;
                }

                // 监控资产校验
                AssetCollectTestVo collectTest = outService
                        .collectTest(BeanUtil.copyProperties(addReq, AssetOutVo.class));
                if (!AssetCollectTestVo.SUCCES_CODE.equals(collectTest.getCode())) {
                    throw new AddAssetException(AddAssetException.COLLECT_ERROR,
                            "指标采集校验失败-采集器返回信息:" + collectTest.getMsg(),
                            Objects.nonNull(collectTest.getTestResultList()) ? collectTest.getTestResultList() : new ArrayList<>());
                }
            }

            // 设置默认阈值(模板导入不设置)
            if (ObjectUtil.isNull(addReq.isTemplate()) || !addReq.isTemplate()) {
                this.setDefaultThreshold(addReq);
            }

            // 资产管理添加的页面因为没有账号密码的信息所以默认不采集
            if (addReq.getWatch() == (byte) 1) {
                // 资产增加通知采集器
                outService.notifyOnChange(OutConst.ADD_ASSET, addReq);
                // 发起路由发现
                routeServ.collectRoute(addReq);

            }
        }
    }

    /**
     * 监控保存资产中监控资产校验
     *
     * @param collectReq
     * @return
     */
    @Override
    public Object verifyIp(AssetCollectReq collectReq) throws AddAssetException {
        String ip = collectReq.getIp();
        boolean ipIsEmpty = StrUtil.isEmpty(ip);
        // 检查IP是否已分配
        String key = "ASSET_VER_IP_TMP_KEY" + ip;
        synchronized (key) {
            if (!ipIsEmpty) {
                Asset one = this.findOneByIp(ip);
                if (Objects.nonNull(one)) {
                    if (StrUtil.isNotEmpty(collectReq.getId()) && !one.getId().equals(collectReq.getId())) {
                        String orgName = getAssetOrgName(one);
                        String msg = String.format("IP[%s]已分配给[%s]的设备[%s]", ip, orgName, one.getName());
                        throw new AddAssetException(AddAssetException.VERIFY_ERROR, msg, null);
                    }
                } else {
                    if (StrUtil.isEmpty(collectReq.getId())) {
                        ResultVo<String> result = ResultVoUtil.success("");
                        QueryWrapper<IpInfo> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("IP", ip);
                        List<IpInfo> ipList = ipInfoService.list(queryWrapper);
                        if (ipList.size() == 0) {
                            result = ResultVoUtil.paramError("该ip不可用,请于IP管理添加网段", String.class);
                        }
                        if (!ResultEnum.SUCCESS.getCode().equals(result.getCode())) {
                            throw new AddAssetException(AddAssetException.VERIFY_ERROR, result.getMsg(), null);
                        }
                    }
                }
            }

            // 验证指标
            if (StatusConst.OK != collectReq.getWatch()) {
                return "未设置监控";
            }

            // 监控资产校验
            AssetCollectTestVo collectTest = outService
                    .collectTest(BeanUtil.copyProperties(collectReq, AssetOutVo.class));
            List<AssetTestResult> testResultList = collectTest.getTestResultList();
            if (Objects.isNull(testResultList)) {
                testResultList = new ArrayList<AssetTestResult>();
            }
            if (!AssetCollectTestVo.SUCCES_CODE.equals(collectTest.getCode())) {
                throw new AddAssetException(AddAssetException.COLLECT_ERROR,
                        "指标采集校验失败-采集器返回信息:" + (StrUtil.isNotEmpty(collectTest.getMsg()) ? collectTest.getMsg() : ""),
                        testResultList);
            }

        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAsset(Asset updateReq) throws AddAssetException {
        if (updateReq.getServiceType() != null && updateReq.getServiceType() == 0) {
            try {
                appServerService.deleteServerPort(updateReq.getId(), null);
            } catch (Exception e) {
                log.error("删除应用服务器端口失败：{}", e.getMessage());
            }
        }
        String ip = updateReq.getIp();
        //ip是否为空
//        boolean ipIsEmpty = StrUtil.isEmpty(updateReq.getIp());
        // 检测IP网段是否配置
//        if (!ipIsEmpty) {
//            Boolean examineIp = ipInfoService.examineIp(ip);
//            if (!examineIp) {
//                throw new AddAssetException(AddAssetException.VERIFY_ERROR, "请先配置IP网段", null);
//            }
//            Asset one = this.findOneByIp(ip);
//            if (Objects.nonNull(one) && !one.getId().equals(updateReq.getId())) {
//                String orgName = getAssetOrgName(one);
//                String msg = String.format("IP[%s]已分配给[%s]" +
//                        "的设备[%s]", ip, orgName, one.getName());
//                throw new AddAssetException(AddAssetException.VERIFY_ERROR, msg, null);
//            }
//        } else {
//            ip = "--";
//        }

        //是否需要监控
        boolean needWatch = (AssetWatchStatusEnum.WATCH_STATUS_YES.getCode() == updateReq.getWatch());
        Asset dbAsset = getById(updateReq.getId());
        if (Objects.isNull(dbAsset)) {
            throw new AddAssetException(AddAssetException.VERIFY_ERROR, "资产ID不存在：" + updateReq.getId(), null);
        }
        //默认不进行采集验证
        updateReq.setBeforeVerify("yes");

        if (!updateReq.getAssetMode().equals(dbAsset.getAssetMode())) {
            updateReq.setBeforeVerify("no");
        }
        if (!ip.equals(dbAsset.getIp())) {
            updateReq.setBeforeVerify("no");
        }

        //选择监控 且 监控相关字段发生改变  需重新采集验证
        if (updateReq.getWatch() == AssetWatchStatusEnum.WATCH_STATUS_YES.getCode() && this.assectChange(dbAsset, updateReq)) {
            updateReq.setBeforeVerify("no");
        }

        // 不监控变为监控  需要采集验证
        if (updateReq.getWatch() == AssetWatchStatusEnum.WATCH_STATUS_YES.getCode() && dbAsset.getWatch() == AssetWatchStatusEnum.WATCH_STATUS_NO.getCode()) {
            updateReq.setBeforeVerify("no");
        }

        // 更新进程
        String dbAssetCode = dbAsset.getAssetCode();
        String assetCode = updateReq.getAssetCode();

        if (StrUtil.isNotEmpty(assetCode) && !assetCode.equals(dbAssetCode)) {
            // 资产编号变动，更新之前的进程模式配置为普通
            thresholdProcessService.updateMode(dbAssetCode, ProcessHostModeEnum.COMMON.getCode());
        }

        // 需要更新topo
        QueryWrapper<TopoVertex> queryWrapper = new QueryWrapper<TopoVertex>();
        queryWrapper.eq("ASSET_ID", updateReq.getId());
        queryWrapper.isNotNull("NAME");
        List<TopoVertex> topoVers = topoVertexServ.list(queryWrapper);
        for (TopoVertex topoVertex : topoVers) {
            topoVertex.setName(updateReq.getName());
        }
        if (!topoVers.isEmpty()) {
            topoVertexServ.updateBatchById(topoVers);
        }
        // 修改资产数据
        assetMapper.updateById(updateReq);

        // 更新保存资产附属信息表
        if (AssetModeConst.TERMINAL.equals(updateReq.getDesk())) {
            updateReq.setCabinetId("");
            updateReq.setStartPosition(null);
            updateReq.setEndPosition(null);
        }
        this.saveOrUpdateAttach(updateReq);

        //修改了组织 并且现在是监控状态
        if ((!updateReq.getOrgId().equals(dbAsset.getOrgId())) && dbAsset.getWatch() == AssetWatchStatusEnum.WATCH_STATUS_YES.getCode()) {
            outService.notifyOnChange(OutConst.DEL_ASSET, updateReq);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            outService.notifyOnChange(OutConst.ADD_ASSET, updateReq);
            // 更新了组织则更新告警信息 20231220
            UpdateWrapper<AlarmInfo> update = Wrappers.update();
            update.set("org_id", updateReq.getOrgId());
            update.set("asset_ip", updateReq.getIp());
            update.eq("asset_id", updateReq.getId());
            alarmInfoService.update(update);
        } else {

            if (!needWatch) {
                //由监控更改为不监控  通知采集器删除任务
                if (dbAsset.getWatch() == AssetWatchStatusEnum.WATCH_STATUS_YES.getCode()) {
                    outService.notifyOnChange(OutConst.DEL_ASSET, updateReq);
                }
                //现改为不监控后 只需更改数据库 且通知采集器关闭任务即可 无需走以下验证逻辑
                return;
            }


            // 资产需要监控 且更改过采集相关字段  需再次验证监控指标
            if (updateReq.getBeforeVerify().equals("no")) {
                AssetOutVo assetOutVo = BeanUtil.copyProperties(updateReq, AssetOutVo.class);
                AssetCollectTestVo collectTest = outService.collectTest(assetOutVo);
                log.info("【指标测试响应信息】：{}", JSONUtil.toJsonStr(collectTest));

                if (!AssetCollectTestVo.SUCCES_CODE.equals(collectTest.getCode())) {
                    throw new AddAssetException(AddAssetException.COLLECT_ERROR, "指标采集校验失败-采集器返回信息:" + collectTest.getMsg(),
                            collectTest.getTestResultList());
                }
            }


            // 由未监控->监控  向采集器增加 采集任务
            if (!dbAsset.getWatch().equals(updateReq.getWatch())) {
                this.notifySubjectV2(updateReq, OutConst.ADD_ASSET);
            } else if (updateReq.getBeforeVerify().equals("no")) {
                // 监控->监控 且采集字段发生改变
                this.notifySubjectV2(updateReq, OutConst.ALL_ASSET_UPDATE);
            }
        }
    }

    /**
     * 资产附属信息
     *
     * @param asset
     */
    private void saveOrUpdateAttach(Asset asset) {
        if (asset.getRoomId() != null) {
            AssetAttach assetAttach = new AssetAttach();
            assetAttach.setAssetId(asset.getId());
            BeanUtils.copyProperties(asset, assetAttach);

            if (AssetModeConst.TERMINAL.equals(asset.getDesk())) {
                assetAttach.setCabinetId("");
                assetAttach.setStartPosition(null);
                assetAttach.setEndPosition(null);
            }
            assetAttachService.saveOrUpdate(assetAttach);
        }
    }

    /**
     * 设置默认阈值
     *
     * @param asset
     */
    private void setDefaultThreshold(Asset asset) {
        List<String> assetIds = ShiroUtil.getSubjectAssetIds();
        ThresholdAssetVo config = thresholdAssetService.queryDefaultConf(assetIds, asset.getAssetMode());

        if (Objects.nonNull(config)) {
            ThresholdAsset entity = new ThresholdAsset();
            BeanUtil.copyProperties(config, entity);
            entity.setAssetId(asset.getId());
            entity.setAutoFlag(ThresholdAutoFlagConst.ORG_THRESHOLD);
            entity.setRunningTimeDeviation(null);
            entity.setAssetMode(asset.getAssetMode());
            if (Arrays.asList(0, 2).contains(asset.getCollectionType()) && Objects.nonNull(config.getRunningTimeDeviationLinux())) {
                entity.setRunningTimeDeviation(config.getRunningTimeDeviationLinux());
            }

            if (Objects.equals(SystemTypeEnum.WINDOWS.getCode(), asset.getCollectionType()) && Objects.nonNull(config.getRunningTimeDeviationWindows())) {
                entity.setRunningTimeDeviation(config.getRunningTimeDeviationWindows());
            }

            thresholdAssetService.save(entity);
        }
    }

    /**
     * 根据ip获取所有绑定资产
     */
    @Override
    public Asset findOneByIp(String ip) {
        return assetMapper.selectByIp(ip);
    }

    /**
     * 根据ip获取资产信息
     */
    @Override
    public AssetMsgVo findMsgByIp(String ip) {
        Asset asset = assetMapper.selectByIp(ip);
        if (Objects.isNull(asset)) {
            return null;
        }

        return getAssetMsgVo(asset);
    }

    @Override
    public AssetMsgVo findMsgById(String id) {
        Asset asset = assetMapper.selectById(id);
        if (Objects.isNull(asset)) {
            return null;
        }

        return getAssetMsgVo(asset);
    }

    @Override
    public AssetMsgVo getAssetMsgVo(Asset asset) {
        Cabinet cabinet = cabinetMapper.findByAssetId(asset.getId());
        SysOrg org = orgService.getById(asset.getOrgId());
        RoomVo room = roomMapper.selectByAssetId(asset.getId());
        AssetAttach attch = assetAttachService.getByAssetId(asset.getId());

        AssetMsgVo copy = EntityBeanUtil.copy(asset, AssetMsgVo.class);
        copy.setAssetName(asset.getName());

        AssetMode mode = assetModeService.getByCode(asset.getDesk());
        if (Objects.nonNull(mode)) {
            copy.setAssetMode(mode.getName());
        } else {
            copy.setAssetMode(asset.getDesk() + "");
        }
        AssetManufacturer manufacturer = assetManufacturerService.getById(asset.getManufacturerId());
        if (Objects.nonNull(manufacturer)) {
            copy.setManufacturerName(manufacturer.getName());
        }
        if (Objects.nonNull(org)) {
            copy.setOrgName(org.getTitle());
        }
        if (Objects.nonNull(room)) {
            copy.setRoomName(room.getName());
        }
        if (Objects.nonNull(cabinet)) {
            copy.setCabinetCode(cabinet.getCode() + "");
            copy.setCabinetName(cabinet.getName());
            copy.setPositionStr(attch.getStartPosition() + "至" + attch.getEndPosition());
        }
        if (Objects.nonNull(asset.getDownlineTime())) {
            long downlineTimeCount = DateUtil.between(new Date(), asset.getDownlineTime(), DateUnit.MS);
            if (downlineTimeCount < 0) {
                downlineTimeCount = 0;
            }
            String formatBetween = DateUtil.formatBetween(downlineTimeCount);
            copy.setDownlineTimeCount(downlineTimeCount);
            copy.setDownlineTimeCountStr(formatBetween);
        }
        return copy;
    }

    /**
     * 删除资产
     *
     * @param assetId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAsset(String assetId) throws Exception {
        Asset asset = this.getById(assetId);
        // 通知外部应用
        this.notifySubjectV2(asset, OutConst.DEL_ASSET);

        return 1;
    }

    /**
     * 根据资产ID获取资产的归属 机柜 机房 组织
     *
     * @param id
     * @return
     */
    @Override
    public AssetBelong findAssetBelongById(String id) {
        if (StrUtil.isEmpty(id)) {
            return null;
        }
        return assetMapper.findAssetBelongById(id);
    }

    /**
     * 按类型统计设备
     *
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getModeAsset(List<String> orgIds) {
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }
        return assetMapper.getModeAsset(orgIds);
    }

    /**
     * 按组织统计设备数量
     *
     * @return
     */
    @Override
    public List<StatisticsAlarmVo> getOrgAsset() {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }
        return assetMapper.getOrgAsset(orgIds);
    }

    /**
     * 获取满足大修提醒的所有设备
     *
     * @return
     */
    @Override
    public List<Asset> getOverhaulList() {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        if (CollectionUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }
        return assetMapper.getOverhaulList(orgIds);
    }

    /**
     * 获取资产Org名称
     *
     * @param one
     * @return
     */
    private String getAssetOrgName(Asset one) {
        String orgName = "";
        SysOrg org = orgService.getById(one.getOrgId());

        String[] pidArr = org.getPids().split(",");
        for (String pid : pidArr) {
            pid = pid.replace("[", "").replace("]", "");
            if ("0".equals(pid)) {
                continue;
            }
            String title = orgService.getById(pid).getTitle();
            if (StrUtil.isNotEmpty(title)) {
                orgName = title + "-";
            }
        }
        orgName += org.getTitle();
        return orgName;
    }

    @Override
    public Asset getOneByAllIp(String ip) {
        Asset asset = assetMapper.selectByIp(ip);
        if (Objects.nonNull(asset)) {
            return asset;
        }
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<Asset>();
        queryWrapper.eq("IP2", ip);
        List<Asset> ip2List = assetMapper.selectList(queryWrapper);
        if (!ip2List.isEmpty()) {
            return ip2List.get(0);
        }
        queryWrapper = new QueryWrapper<Asset>();
        queryWrapper.eq("IPMI_IP", ip);
        List<Asset> assetList = assetMapper.selectList(queryWrapper);

        if (!assetList.isEmpty()) {
            return assetList.get(0);
        }

        return null;
    }

    /**
     * 机柜中可用U位 syt
     *
     * @param cabinetId
     * @return
     */
    @Override
    public List<CabinetUsed> freePosition(String cabinetId) {
        // 查询该机柜中已占用的U位
        QueryWrapper<AssetAttach> query = Wrappers.query();
        query.eq("CABINET_ID", cabinetId);
        List<AssetAttach> usedPositionList = assetAttachService.list(query);

        // 已使用的U位
        List<Integer> used = new ArrayList<>();
        for (AssetAttach assetAttach : usedPositionList) {
            // 开始位置不小于1,结束位置不大于42
            if (assetAttach.getStartPosition() < 1 || assetAttach.getEndPosition() > 42) {
                return null;
            }
            // 开始位置与结束位置相同直接添加进已使用集合
            if (assetAttach.getStartPosition() == assetAttach.getEndPosition()) {
                used.add(assetAttach.getStartPosition());
            }
            // 开始位置与结束位置不同的,遍历出开始与结束位置之间的所有U位数。然后逐个添加进已使用集合
            if (!assetAttach.getStartPosition().equals(assetAttach.getEndPosition())) {
                List<Integer> allInt = this.getAllInt(assetAttach.getStartPosition(), assetAttach.getEndPosition());
                used.addAll(allInt);
            }
        }

        // 升序排序
        Collections.sort(used);
        // 去重
        List<Integer> uesdUnrepeat = used.stream().distinct().collect(Collectors.toList());

        // 初始化一个42U机柜
        List<CabinetUsed> cabinetUseds = new ArrayList<>();
        for (int i = 0; i < 42; i++) {
            CabinetUsed cu = new CabinetUsed();
            cu.setPositon(i + 1);
            cu.setUsedState(false);
            cabinetUseds.add(cu);
        }

        // 将已占用的U位标记出来
        for (int i = 0; i < 42; i++) {
            for (int y = 0; y < uesdUnrepeat.size(); y++) {
                if (cabinetUseds.get(i).getPositon() == uesdUnrepeat.get(y)) {
                    cabinetUseds.get(i).setUsedState(true);
                }
            }
        }
        return cabinetUseds;
    }

    // 获取两个整数之间的所有整数,包含首尾数 syt
    private List<Integer> getAllInt(Integer startPosition, Integer endPosition) {
        List<Integer> list = new ArrayList<>();
        for (; startPosition <= endPosition; startPosition++) {
            list.add(startPosition);
        }
        return list;
    }

    /**
     * 资产IP地址ping测试 syt
     *
     * @param
     */
    @Override
    public ResultVo<?> assetPing(String id) {
        // 返回的结果集
        List<AssetPingVo> res = new ArrayList<AssetPingVo>();
        Asset asset = assetMapper.selectById(id);

        if (Objects.isNull(asset)) {
            AssetPingVo assetPingVo = new AssetPingVo();
            assetPingVo.setRes("非系统内设备");
            res.add(assetPingVo);
            return ResultVoUtil.success("非系统内设备", res);
        }

        AssetPingVo assetPingVo = new AssetPingVo();
        RealTimePingStatusEnum result = null;
        try {
            result = collectAgent.realTimePing(asset);
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ASSET_MANAGE, asset.getIp(), e);
            assetPingVo.setRes("ping发生异常，请重试");
            res.add(assetPingVo);
            return ResultVoUtil.success("The results of the ping test", res);
        }

        if (RealTimePingStatusEnum.DOWN == result) {
            assetPingVo.setRes(IpPingStatusEnum.PING_NO.getMsg());
        } else if (RealTimePingStatusEnum.UP == result) {
            assetPingVo.setRes(IpPingStatusEnum.PING_YES.getMsg());
        } else {
            assetPingVo.setRes("未知");
        }

        res.add(assetPingVo);

        return ResultVoUtil.success("The results of the ping test", res);
    }

    /**
     * telnet测试主机端口是否启用 syt
     */
    @Override
    public ResultVo<?> assetTelnet(AssetNetReq telnetReq) {
        // 待测试的
        List<AssetTelnet> reqs = telnetReq.getTelnet();
        AssetTelnet req = reqs.get(0);
        // 返回模型
        AssetTelnetVo telnetVo = new AssetTelnetVo();

        // 转化中文标点符号到英文 syt 2021/6/16
        req.setPort(ToolUtil.chineseToEnglish(req.getPort()));

        try {
            this.testNullPortOrOffline(req);
        } catch (AssetTelnetException e) {
            telnetVo.setRes(e.getTelnetErrorMsg());
            return ResultVoUtil.success("telnet测试结果", telnetVo);
        }

        // port根据,拆分格式:端口号1,端口号2,区间1-区间2区间差小于等于30
        String[] portArr = req.getPort().split(",");

        // 测试结果
        StringBuilder success = new StringBuilder();
        StringBuilder fail = new StringBuilder();

        for (int i = 0; i < portArr.length; i++) {
            String portItem = portArr[i];
            if ("".equals(portItem)) {
                log.info("telnet测试输入端口{}范围有误,端口为整数,范围为0-65535。", req.getPort());
                telnetVo.setRes("输入的端口" + portItem + "检测范围有误，端口为整数,范围为0-65535");
                telnetVo.setIp(req.getIp());
                telnetVo.setPort(req.getPort());
                return ResultVoUtil.success("telnet测试结果", telnetVo);
            }
            try {
                telnetVo = this.telnetPort(portItem, req, success, fail);
            } catch (AssetTelnetException ate) {
                log.error("telnet测试异常: {}", ate.getTelnetErrorMsg());
                telnetVo.setRes(ate.getTelnetErrorMsg());
                return ResultVoUtil.success("telnet测试结果", telnetVo);
            }
        }

        String msg = "";
        if (StrUtil.isNotEmpty(success.toString())) {
            msg = "<br/>" + "可用端口: " + success.toString();
        }
        if (StrUtil.isNotEmpty(fail.toString())) {
            msg += "<br/>" + "不可用端口: " + fail.toString();
        }
        telnetVo.setRes((StrUtil.isNotEmpty(telnetVo.getRes()) ? telnetVo.getRes() + "," : "") + msg);

        return ResultVoUtil.success("telnet测试结果", telnetVo);
    }

    /**
     * telnet 测试
     *
     * @param portItem
     * @param req
     * @return
     * @throws AssetTelnetException
     */
    private AssetTelnetVo telnetPort(String portItem, AssetTelnet req, StringBuilder success, StringBuilder fail)
            throws AssetTelnetException {

        // 返回模型
        AssetTelnetVo telnetVo = new AssetTelnetVo();
        if (portItem.contains("-")) {
            int[] portList = this.getPortList(portItem);
            // 根据区间去telnet
            for (int j = 0; j < portList.length; j++) {
                try {
                    this.verifyPort(portList[0] + "", portList[portList.length - 1] + "", req);
                } catch (AssetTelnetException ate) {
                    telnetVo.setRes(ate.getTelnetErrorMsg());
                    return telnetVo;
                }

                int testPort = portList[j];
                Boolean testOk = telnetClient(req.getIp(), testPort);
                if (testOk) {
                    success.append(testPort).append(",");
                } else {
                    fail.append(testPort).append(",");
                }
            }
        } else {
            // 单个telnet
            try {
                this.verifyPort(portItem, null, req);
            } catch (AssetTelnetException ate) {
                telnetVo.setRes(ate.getTelnetErrorMsg());
                return telnetVo;
            }

            Boolean testOk = telnetClient(req.getIp(), Integer.parseInt(portItem));
            if (testOk) {
                success.append(portItem).append(",");
            } else {
                fail.append(portItem).append(",");
            }
        }

        return telnetVo;
    }

    /**
     * 测试端口
     *
     * @param ip
     * @param port
     * @return
     */
    private Boolean telnetClient(String ip, int port) {
        TelnetClient telnetClient = null;
        try {
            telnetClient = new TelnetClient("vt200"); // 指明Telnet终端类型，否则会返回来的数据中文会乱码
            telnetClient.setDefaultTimeout(1000); // socket延迟时间：5000ms
            telnetClient.connect(ip, port); // 建立一个连接,默认端口是23
            return true;
        } catch (Exception e) {
            if (LogInputUtils.inputError(ServerTypeEnum.ASSET)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.ASSET, ErrorCodeEnum.ASSET_TELNET_ERROR, ip, e.getMessage()), e);
            }
            return false;
        } finally {
            if (Objects.nonNull(telnetClient)) {
                try {
                    telnetClient.disconnect();
                } catch (IOException e) {
                    if (LogInputUtils.inputError(ServerTypeEnum.ASSET)) {
                        log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.ASSET, ErrorCodeEnum.ASSET_TELNET_CLOSE_ERROR, ip, e.getMessage()), e);
                    }
                }
            }
        }
    }

    /**
     * 验证:IP的端口为空不能检测,验证:离线设备不能检测
     *
     * @param req: AssetTelnet
     * @Author: syt
     */
    private void testNullPortOrOffline(AssetTelnet req) throws AssetTelnetException {
        if (StrUtil.isEmpty(req.getIp()) || StrUtil.isEmpty(req.getPort())) {
            throw new AssetTelnetException(AssetTelnetException.SCOPE_ERROR, "IP地址或端口为空无法检测");
        }
        Asset dbAsset = assetMapper.selectByIp(req.getIp());
        if (Objects.nonNull(dbAsset)) {
            if (dbAsset.getStatus() == 0) {
                throw new AssetTelnetException(AssetTelnetException.ASSET_ERROR, "设备离线无法检测");
            }
        }
    }

    /**
     * 端口的区间检测长度不得超过30 并获取区间内所有端口号
     *
     * @Author: syt
     */
    private int[] getPortList(String port) throws AssetTelnetException {
        String[] portArr1 = port.split("-");

        String startPort = portArr1[0];
        String endPort = portArr1[1];

        if (portArr1.length != 2 || "".equals(startPort) || "".equals(endPort) || !NumberUtil.isNumber(startPort)
                || !NumberUtil.isNumber(endPort)) {
            throw new AssetTelnetException(AssetTelnetException.FORMAT_ERROR, "请输入正确格式");
        }

        Integer left = Integer.parseInt(startPort);
        Integer right = Integer.parseInt(endPort);

        int[] sortPort = Arrays.stream(NumberUtil.range(left, right)).sorted().toArray();

        // 区间数据大于30
        if (sortPort.length > 30) {
            throw new AssetTelnetException(AssetTelnetException.SCOPE_ERROR, "检测区间不能超过30");
        }

        return sortPort;
    }

    /***
     * 监测端口范围不得在0-65535外,分为区间监测和单个端口监测
     *
     * @param start:       区间开始端口号
     * @param end:         区间结束端口号
     * @param 'scope'是区间检测 'single'是单个端口监测
     * @param
     * @param req:
     * @Author: syt
     */
    private void verifyPort(String start, String end, AssetTelnet req) throws AssetTelnetException {
        if (StrUtil.isNotEmpty(start)) {
            if (!NumberUtil.isNumber(start)) {
                throw new AssetTelnetException(AssetTelnetException.SCOPE_ERROR, "输入的端口" + start + "非数字");
            }
            int parseInt = Integer.parseInt(start);

            if (parseInt > 65535 || parseInt < 0) {
                throw new AssetTelnetException(AssetTelnetException.SCOPE_ERROR,
                        "输入的端口" + start + "检测范围有误，端口为整数,范围为0-65535");
            }
        }

        if (StrUtil.isNotEmpty(end)) {
            if (!NumberUtil.isNumber(end)) {
                throw new AssetTelnetException(AssetTelnetException.SCOPE_ERROR, "输入的端口" + end + "非数字");
            }
            int parseInt = Integer.parseInt(end);

            if (parseInt > 65535 || parseInt < 0) {
                throw new AssetTelnetException(AssetTelnetException.SCOPE_ERROR,
                        "输入的端口" + end + "检测范围有误，端口为整数,范围为0-65535");
            }
        }

    }

    @Override
    public List<Asset> listByAssetCode(String assetCode) {
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<Asset>();
        queryWrapper.eq("ASSET_CODE", assetCode);
        queryWrapper.eq("IS_DEL", AssetStatusEnum.ASSET_STATUS_ONLINE.getCode());
        return list(queryWrapper);
    }

    @Override
    public List<Asset> getCenterNetworklList() {
        return assetMapper.getCenterNetworklList();
    }

    @Override
    public Boolean isStationAsset(String assetId) {
        Asset asset = getById(assetId);
        if (Objects.isNull(asset)) {
            return false;
        }

        SysOrg org = orgService.getById(asset.getOrgId());
        return OrgTypeEnum.STATION.getCode() == org.getType().byteValue();
    }

    /**
     * 根据资产code获取资产
     *
     * @param assetCode
     * @return
     */
    @Override
    public List<Asset> findGroupAssetByCode(String assetCode) {
        return assetMapper.findGroupAssetByCode(assetCode);
    }

    /**
     * 根据组织ID列表获取相应属性资产
     *
     * @param orgIdList
     * @return
     */
    @Override
    public List<Asset> ListByOrgIds(List<String> orgIdList) {
        List<Asset> assets = new ArrayList<>();
        for (String orgId : orgIdList) {
            assets.addAll(assetMapper.findAssetByOrgId(orgId));
        }
        return assets;
    }

    /**
     * 检测设备的监控相关字段是否发生改变  发生后需验证采集 并更改采集任务
     * a1 :newAsset a2 :oldAsset
     * 有改动返回 true
     */
    public boolean assectChange(Asset a1, Asset a2) {

        if (this.compare(a1.getManufacturerId(), a2.getManufacturerId())) {
            return true;
        }
        if (this.compare(a1.getAssetMode(), a2.getAssetMode())) {
            return true;
        }
        if (this.compare(a1.getNtpFlag(), a2.getNtpFlag())) {
            return true;
        }
        if (this.compare(a1.getOsUser(), a2.getOsUser())) {
            return true;
        }
        if (this.compare(a1.getOsPassword(), a2.getOsPassword())) {
            return true;
        }
        if (this.compare(a1.getManufacturerId(), a2.getManufacturerId())) {
            return true;
        }
        Integer collectionType = a2.getCollectionType();
        if (this.compare(a1.getCollectionType(), collectionType)) {
            return true;
        }

        if (ObjectUtil.isNotNull(collectionType)
                && (SystemTypeEnum.LINUX.getCode().intValue() == collectionType || SystemTypeEnum.AIX.getCode().intValue() == collectionType)) {
            return this.compare(a1.getLoginName(), a2.getLoginName()) || this.compare(a1.getLoginPwd(), a2.getLoginPwd());
        }
        return false;

    }

    /**
     * 比较属性值  属性值变化或者置空
     * 有改动返回 true
     */
    private boolean compare(Object s1, Object s2) {
        String str1 = "";
        String str2 = "";
        if (ObjectUtil.isNotNull(s1)) {
            str1 = s1.toString();
        }
        if (ObjectUtil.isNotNull(s2)) {
            str2 = s2.toString();
        }

        return !str1.equals(str2);
    }

    @Override
    public Double queryHealth() {
        Double queryHealth = assetMapper.queryHealth();
        if (Objects.isNull(queryHealth)) {
            return 100d;
        }
        return queryHealth;
    }

    @Override
    public List<Asset> listByCollectionType(List<Integer> asList) {
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<Asset>();
        queryWrapper.in("COLLECTION_TYPE", asList);
        queryWrapper.eq("IS_DEL", StatusEnum.OK.getCode());
        return list(queryWrapper);
    }

    /**
     * 查询网络设备数据的最后一次采集时间
     *
     * @return
     */
    @Override
    public Date queryNetLastTimeDate(String assetId) {
        // cpu、内存、端口
        List<CollectCpu> realTimeData = cpuService.getRealTimeData(assetId);
        if (!realTimeData.isEmpty()) {
            return realTimeData.get(0).getCollectTime();
        }
        List<CollectMemory> memoryList = memoryServ.getRealTimeData(assetId);
        if (!memoryList.isEmpty()) {
            return memoryList.get(0).getCollectTime();
        }
        List<CollectInterfaces> interfacesList = interfacesServ.getRealTimeData(assetId);
        if (!interfacesList.isEmpty()) {
            return interfacesList.get(0).getCollectTime();
        }

        return null;
    }


    /**
     * 查询服务器数据的最后一次采集时间
     *
     * @return
     */
    @Override
    public Date queryServerLastTimeDate(String assetId) {
        //cpu、磁盘、内存、网卡、进程
        List<CollectCpu> realTimeData = cpuService.getRealTimeData(assetId);
        if (!realTimeData.isEmpty()) {
            return realTimeData.get(0).getCollectTime();
        }
        List<CollectDisk> diskList = diskService.getRealTimeData(assetId);
        if (!diskList.isEmpty()) {
            return diskList.get(0).getCollectTime();
        }
        List<CollectNetworkCard> netCardList = netCardServ.getRealTimeData(assetId);
        if (!netCardList.isEmpty()) {
            return netCardList.get(0).getCollectTime();
        }
        List<CollectMemory> memoryList = memoryServ.getRealTimeData(assetId);
        if (!memoryList.isEmpty()) {
            return memoryList.get(0).getCollectTime();
        }
        List<CollectRaid> capacities = raidService.findByType(assetId, null, 3);
        if (ObjectUtil.isNotNull(capacities) && !capacities.isEmpty()) {
            return capacities.get(0).getCollectTime();
        }
        Date lastTime = dsService.findLastTime(assetId);
        if (Objects.nonNull(lastTime)) {
            return lastTime;
        }

        return null;
    }

    @Override
    public List<String> listJccaId() {
        return assetMapper.selectJccaAsset();
    }

    @Override
    public String getUIndex(String assetId) {
        return assetMapper.getUIndex(assetId);
    }

    @Override
    public void updateMonitorStatus(Asset asset, Integer status) {
        assetMapper.updateMonitorStatus(asset.getId(), status);
    }


    /**
     * @description: 被监控设备总数
     * @author: HanHW
     * @date: 2023/10/25 15:20
     * @param: []
     * @return: java.lang.Integer
     **/
    @Override
    public Integer getAssetCountV2() {
        QueryWrapper<Asset> wrapper = Wrappers.query();
        wrapper.eq("IS_DEL", StatusEnum.OK.getCode());
        wrapper.notLike("DESK", ApiAssetController.ORTHER_MODEL_FLAG);
        SysConfig sysConfig = sysModuleConfigService.getSysConfig();
        String showJcca = sysConfig.getShowJcca();
        if ("no".equals(showJcca)) {
            wrapper.and(w -> w.notLike("ASSET_SUPPLIER", BusinessTypeEnums.JCCA.name()).or().isNull("ASSET_SUPPLIER"));
        }
//        wrapper.eq("WATCH", StatusEnum.OK.getCode());
        return this.count(wrapper);
    }

    /**
     * @description: 按组织类型统计被监控设备数量
     * @author: HanHW
     * @date: 2023/10/25 15:48
     * @param: [OrgTypeConst]
     * @return: java.lang.Integer
     **/
    @Override
    public Integer getAssetCountByOrgTypeV2(Byte orgType) {
        List<SysOrg> centerOrgList = orgService.getListByOrgType(orgType);
        List<String> ids = centerOrgList.stream().map(SysOrg::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        QueryWrapper<Asset> wrapper = Wrappers.query();
        wrapper.eq("IS_DEL", StatusEnum.OK.getCode());
        wrapper.notLike("DESK", ApiAssetController.ORTHER_MODEL_FLAG);
//        wrapper.eq("WATCH", StatusEnum.OK.getCode());
        wrapper.in("ORG_ID", ids);
        SysConfig sysConfig = sysModuleConfigService.getSysConfig();
        String showJcca = sysConfig.getShowJcca();
        if ("no".equals(showJcca)) {
            wrapper.and(w -> w.notLike("ASSET_SUPPLIER", BusinessTypeEnums.JCCA.name()).or().isNull("ASSET_SUPPLIER"));
        }
        return this.count(wrapper);
    }


    @Override
    public CabinetAssetInfoVo findCabinetAssetInfoV2(String assetId) {

        return assetMapper.selectCabinetAssetInfoV2(assetId);
    }


    @Override
    public List<String> listIdByUserNameV2(String username, Integer watch) {
        return assetMapper.listIdByUserNameV2(username, watch);
    }


    @Override
    public AssetInfoBaseVo queryBaseInfoV2(String assetId) {
        AssetInfoBaseVo assetInfoBaseVo = assetMapper.queryBaseInfoV2(assetId);
        if (StrUtil.isNotEmpty(assetInfoBaseVo.getAssetImagePath())) {
            //拼接正确访问路径
            assetInfoBaseVo.setAssetImagePath(staticUrl + assetInfoBaseVo.getAssetImagePath());
        }
        //查询最后一次采集时间
        if (Objects.nonNull(assetInfoBaseVo)) {
            Date date = queryServerLastTimeDate(assetId);
            assetInfoBaseVo.setCollectTime(date);
        }

        return assetInfoBaseVo;
    }

    /**
     * @description: 查询所有监控设备
     * @author: HanHW
     * @date: 2023/11/16 13:20
     * @param: []
     * @return: java.util.List<com.jcca.web.asset.entity.Asset>
     **/
    @Override
    public List<Asset> listAllV2() {
        return assetMapper.listAllV2();
    }

    /**
     * @description: 添加或更新资产
     * @author: HanHW
     * @date: 2023/12/5 17:31
     * @param: [asset]
     * @return: void
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAssetV2(Asset req) throws Exception {
        if (req.getServiceType() != null && req.getServiceType() == 1) {
            if (req.getCollectionType() == null || req.getCollectionType() != 0) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "只有linux设备可以配置为应用服务器");
            }
            String orgId = req.getOrgId();
            SysOrg org = orgService.getById(orgId);
            if (org.getType() != OrgTypeConst.CENTER) {
                throw new ResultException(ResultEnum.PARAM_ERROR.getCode(), "只有中心设备可以配置为应用服务器");
            }
        }
        if (req.getServiceType() != null && req.getServiceType() == 0 && !StringUtils.isEmpty(req.getId())) {
            try {
                appServerService.deleteServerPort(req.getId(), null);
            } catch (Exception e) {
                log.error("删除应用服务器端口失败：{}", e.getMessage());
            }
        }

        // 填充类型型号
        this.setAssetMode(req);

//        Asset asset = new Asset();
//        BeanUtils.copyProperties(req, asset);
        // 验证U位
        this.checkUnit(req);

        // 验证上下架时间
        this.checkTime(req);

        // 其它设备单独处理
        String assetModelStr = req.getAssetMode() + "";
        if (assetModelStr.startsWith(ApiAssetController.ORTHER_MODEL_FLAG)) {
            req.setWatch(StatusConst.NO);
            req.setShowTopo(ShowTopoEnum.NOT_SHOW.getCode());
            req.setNtpFlag(NtpFlagEnum.NTP_NO.getCode());

            this.saveOrUpdate(req);
            this.saveOrUpdateAttach(req);
            return;
        }
        // 设置在线状态
        this.setAssetStatus(req);

        // 验证IP
        this.checkIp(req);

        // 设置密码
        this.setPassWord(req);

        // 设置采集类型
        Integer collectionType = req.getCollectionType();
        if (Objects.isNull(collectionType)) {
            req.setCollectionType(SystemTypeEnum.SWITCH_ROUTER.getCode());
        }

        // 修改
        if (!StringUtils.isEmpty(req.getId())) {
            // 检查是否修改了IP
            this.examIp(req);
            try {
                this.updateAssetV2(req);
            } catch (AddAssetException e) {
                throw new ResultException(ResultEnum.WARNING.getCode(), e.getAddErrorMsg());
            }
            return;
        }
        // 新增
        req.setId(MyIdUtil.getId());

        // 采集验证 snmp验证.1采集，SSH验证登录成功，TELENET验证登录
        this.collectTest(req);

        // 保存资产
        this.save(req);
        this.saveOrUpdateAttach(req);

        // 通知其它应用
        req.setId(req.getId());
        this.notifySubjectV2(req, OutConst.ADD_ASSET);
    }

    private void updateAssetV2(Asset req) throws AddAssetException {
        if (req.getServiceType() != null && req.getServiceType() == 0) {
            try {
                appServerService.deleteServerPort(req.getId(), null);
            } catch (Exception e) {
                log.error("删除应用服务器端口失败：{}", e.getMessage());
            }
        }
        String id = req.getId();
        Asset oldAsset = this.getById(id);
        if (Objects.isNull(oldAsset)) {
            throw new AddAssetException(AddAssetException.VERIFY_ERROR, "资产不存在,ID：" + id, null);
        }
        String oip = oldAsset.getIp();
        String oip2 = oldAsset.getIp2();

        // 更新进程
        String oAssetCode = req.getAssetCode();
        String assetCode = req.getAssetCode();
        if (StrUtil.isNotEmpty(assetCode) && !assetCode.equals(oAssetCode)) {
            thresholdProcessService.updateMode(oAssetCode, ProcessHostModeEnum.COMMON.getCode());
        }

        // 需要更新topo
        QueryWrapper<TopoVertex> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ASSET_ID", id);
        queryWrapper.isNotNull("NAME");
        List<TopoVertex> topoVers = topoVertexServ.list(queryWrapper);
        for (TopoVertex topoVertex : topoVers) {
            topoVertex.setName(req.getName());
        }
        if (!topoVers.isEmpty()) {
            topoVertexServ.updateBatchById(topoVers);
        }
        // 修改资产数据
        assetMapper.updateById(req);

        // 更新保存资产附属信息表
        if (AssetModeConst.TERMINAL.equals(req.getDesk())) {
            req.setCabinetId("");
            req.setStartPosition(null);
            req.setEndPosition(null);
        }
        this.saveOrUpdateAttach(req);

        //修改了组织 并且现在是监控状态
        if ((!req.getOrgId().equals(oldAsset.getOrgId())) && oldAsset.getWatch() == AssetWatchStatusEnum.WATCH_STATUS_YES.getCode()) {
            outService.notifyOnChange(OutConst.DEL_ASSET, req);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            outService.notifyOnChange(OutConst.ADD_ASSET, req);

            UpdateWrapper<AlarmInfo> update = Wrappers.update();
            update.set("org_id", req.getOrgId());
            update.set("asset_ip", req.getIp());
            update.eq("asset_id", req.getId());
            alarmInfoService.update(update);
        } else {
            byte owatch = oldAsset.getWatch();
            byte watch = req.getWatch();
            if (owatch == AssetWatchStatusEnum.WATCH_STATUS_NO.getCode() && watch == AssetWatchStatusEnum.WATCH_STATUS_YES.getCode()) {
                outService.notifyOnChange(OutConst.ADD_ASSET, req);
                return;
            }

            // 资产需要监控 且更改过采集相关字段  需再次验证监控指标
            if (watch == AssetWatchStatusEnum.WATCH_STATUS_YES.getCode() && this.assectChange(req, oldAsset)) {
                AssetOutVo assetOutVo = BeanUtil.copyProperties(req, AssetOutVo.class);
                AssetCollectTestVo collectTest = outService.collectTest(assetOutVo);
                AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_MANAGE, "指标测试响应信息:" + oip, collectTest);
                if (!AssetCollectTestVo.SUCCES_CODE.equals(collectTest.getCode())) {
                    throw new AddAssetException(AddAssetException.COLLECT_ERROR, "指标采集校验失败-采集器返回信息:" + collectTest.getMsg(),
                            collectTest.getTestResultList());
                }
            }

            // 监控->不监控  向采集器增加 采集任务
            if (owatch != watch) {
                this.notifySubjectV2(req, OutConst.DEL_ASSET);
            } else if (this.assectChange(req, oldAsset)) {
                // 由监控->监控
                this.notifySubjectV2(req, OutConst.ALL_ASSET_UPDATE);
            }
        }

        // 资产修改通知3D机房
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                this.notifySubjectV2(req, OutConst.ALL_ASSET_UPDATE);
            } catch (AddAssetException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.ASSET_CHANGE, "修改资产通知3D机房异常", e);
            }
        });
    }

    private void examIp(Asset req) {
        String id = req.getId();
        Asset one = this.getById(id);
        String nip = req.getIp();
        String nip2 = req.getIp2();
        String oip = one.getIp();
        String oip2 = one.getIp2();
        if (StringUtils.isEmpty(nip) || StringUtils.isEmpty(oip)) {
            return;
        }
        if (!nip.equals(oip) && !nip.equals(oip2)) {
            // 释放oip  占用nip
            try {
                ipInfoService.liberateIp(oip);
            } catch (AddAssetException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.ASSET_MANAGE, oip, e);
            }
            ipInfoService.allocationIp(nip);
        }

        if (!StringUtils.isEmpty(nip2)) {
            if (!nip2.equals(oip) && !nip2.equals(oip2)) {
                // 释放oip2  占用nip2
                try {
                    ipInfoService.liberateIp(oip2);
                } catch (AddAssetException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.ASSET_MANAGE, oip2, e);
                }
                ipInfoService.allocationIp(nip2);
            }
        }
    }

    private void setAssetMode(Asset req) {
        Integer desk = req.getDesk();
        AssetMode mode = assetModeService.getByCode(desk);
        req.setAssetMode(mode.getAmode());
        req.setDesk(mode.getCode());
    }

    private void collectTest(Asset asset) {
        if (StatusConst.OK != asset.getWatch()) {
            return;
        }
        AssetOutVo assetOutVo = new AssetOutVo();
        BeanUtils.copyProperties(asset, assetOutVo);

        AssetCollectTestVo collectTest = outService.collectTest(assetOutVo);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, asset.getName() + "指标测试响应信息", collectTest);
        if (!AssetCollectTestVo.SUCCES_CODE.equals(collectTest.getCode())) {
            throw new ResultException(ResultEnum.OUT_COLLECT_TEST_ERROR.getCode(), "指标采集校验失败-采集器返回信息:" + collectTest.getMsg());
        }
    }

    private void setPassWord(Asset asset) {
        if (Objects.isNull(asset.getDesk())) {
            asset.setDesk(asset.getAssetMode());
        }
        if (Objects.isNull(asset.getWatch())) {
            asset.setWatch(StatusConst.NO);
        }
        asset.setOsPassword(EncryptUtil.aesEncryptHex(asset.getOsPassword()));
        if (StrUtil.isNotEmpty(asset.getLoginPwd())) {
            asset.setLoginPwd(EncryptUtil.aesEncryptHex(asset.getLoginPwd()));
        }
        if (StrUtil.isNotEmpty(asset.getIpmiPwd())) {
            asset.setIpmiPwd(EncryptUtil.aesEncryptHex(asset.getIpmiPwd()));
        }
    }

    private void checkIp(Asset req) {
        String modeStr = String.valueOf(req.getAssetMode());
        if (modeStr.startsWith(ApiAssetController.ORTHER_MODEL_FLAG)) {
            return;
        }

        String ip = req.getIp();
        String ip2 = req.getIp2();

        if (StringUtils.isEmpty(ip)) {
            throw new ResultException(Integer.parseInt(AssetCollectTestVo.ERRO_CODE), "资产IP1不能为空");
        }

        if (Objects.equals(ip, ip2)) {
            throw new ResultException(Integer.parseInt(AssetCollectTestVo.ERRO_CODE), "资产两个IP不能相同");
        }

        if (!IPAddressUtil.isIPv4LiteralAddress(ip)) {
            throw new ResultException(Integer.parseInt(AssetCollectTestVo.ERRO_CODE), "资产IP1格式不对");
        }
        if (!StringUtils.isEmpty(ip2) && !IPAddressUtil.isIPv4LiteralAddress(ip2)) {
            throw new ResultException(Integer.parseInt(AssetCollectTestVo.ERRO_CODE), "资产IP2格式不对");
        }

        Asset oldasset = this.getOneByAllIp(ip);
        if (Objects.nonNull(oldasset) && StringUtils.isEmpty(req.getId())) {
            throw new ResultException(Integer.parseInt(AssetCollectTestVo.ERRO_CODE), "资产IP1已经被其他设备占用");
        }

        if (Objects.nonNull(oldasset) && !StringUtils.isEmpty(req.getId()) && !oldasset.getId().equals(req.getId())) {
            throw new ResultException(Integer.parseInt(AssetCollectTestVo.ERRO_CODE), "资产IP1已经被其他设备占用");
        }

        if (!StringUtils.isEmpty(ip2)) {
            oldasset = this.getOneByAllIp(ip2);
            if (Objects.nonNull(oldasset) && StringUtils.isEmpty(req.getId())) {
                throw new ResultException(Integer.parseInt(AssetCollectTestVo.ERRO_CODE), "资产IP2已经被其他设备占用");
            }

            if (Objects.nonNull(oldasset) && !StringUtils.isEmpty(req.getId()) && !oldasset.getId().equals(req.getId())) {
                throw new ResultException(Integer.parseInt(AssetCollectTestVo.ERRO_CODE), "资产IP2已经被其他设备占用");
            }
        }

        // 设置IP占用情况 但新版添加资产不关联IP管理 因此此处逻辑不通 TODO

    }

    private void checkTime(Asset asset) {
        Date downlineTime = asset.getDownlineTime();
        Date onlineTime = asset.getOnlineTime();
        if (Objects.nonNull(downlineTime) && Objects.nonNull(onlineTime)) {
            if (downlineTime.before(onlineTime)) {
                throw new ResultException(Integer.parseInt(AssetCollectTestVo.ERRO_CODE), "上架时间不应晚于下架时间");
            }
        }
    }

    /**
     * @description: 资产变动通知其它应用
     * @author: HanHW
     * @date: 2023/12/6 9:21
     * @param: [asset, state]
     * @return: void
     * <p>
     * OutConst 0新增，1删除，2修改
     */
    @Override
    public void notifySubjectV2(Asset asset, Integer state) throws AddAssetException {
        NotifyDelAssetImpl delAsset = SpringContextUtil.getBean(NotifyDelAssetImpl.class);
        delAsset.assetChange(asset, state);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, asset.getIp(), "资产修改成功");

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        for (AssetNotifyService notifyService : notifyServiceList) {
            executorService.execute(() -> {
                try {
                    notifyService.assetChange(asset, state);
                } catch (AddAssetException e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.ASSET_CHANGE, "资产变动通知外部应用异常：" + asset.getIp(), e);
                }
            });
        }
    }

    private void checkUnit(Asset asset) {
        if (!AssetModeConst.HVAC.equals(asset.getDesk()) && !AssetModeConst.UPS.equals(asset.getDesk())) {
            if (!AssetModeConst.TERMINAL.equals(asset.getDesk()) && StrUtil.isEmpty(asset.getCabinetId())) {
                throw new ResultException(Integer.parseInt(AssetCollectTestVo.ERRO_CODE), "非终端设备需要选择所属机柜");
            }
        }

        String cabinetId = asset.getCabinetId();
        String assetId = asset.getId();
        if (StrUtil.isEmpty(cabinetId)) {
            return;
        }
        Integer startPosition = asset.getStartPosition();
        Integer endPosition = asset.getEndPosition();
        if (Objects.isNull(startPosition)) {
            throw new ResultException(Integer.parseInt(AssetCollectTestVo.ERRO_CODE), "请确认设备起始位置");
        }

        // 校验机柜中设备位置
        if (Integer.compare(startPosition, endPosition) == 1) {
            throw new ResultException(Integer.parseInt(AssetCollectTestVo.ERRO_CODE), "起始位置应小于等于结束位置");
        }
        // 同一机柜中U位不能重叠
        QueryWrapper<AssetAttach> query = Wrappers.query();
        query.eq("cabinet_id", cabinetId);
        if (Objects.nonNull(assetId)) {
            query.ne("asset_id", assetId);
        }
        List<AssetAttach> assetAttachList = assetAttachService.list(query);
        for (AssetAttach attach : assetAttachList) {
            int start = attach.getStartPosition();
            int end = attach.getEndPosition();
            if ((startPosition >= start && startPosition <= end) || (endPosition >= start && endPosition <= end)
                    || (startPosition < start && endPosition > end)) {
                throw new ResultException(Integer.parseInt(AssetCollectTestVo.ERRO_CODE), "设备位置与机柜内已有U位重叠");
            }
        }
    }

    private void setAssetStatus(Asset asset) {
        if (Objects.isNull(asset.getPort())) {
            asset.setPort(Asset.getDefaultPort(asset.getCollectionType()));
        }
        // 当选择不监控时 把设备监控状态状态 初始化为不监控
        if (Objects.isNull(asset.getWatch())) {
            asset.setWatch(AssetWatchStatusEnum.WATCH_STATUS_NO.getCode());
        }
        if (asset.getWatch() == AssetWatchStatusEnum.WATCH_STATUS_NO.getCode()) {
            asset.setStatus(AssetStatusEnum.ASSET_STATUS_NO_WATCH.getCode());
            asset.setMonitor(AssetMonitorEnum.UNMONITOR.code);
            return;
        }
        // 选择监控 监控状态默认在线
        asset.setStatus(AssetStatusEnum.ASSET_STATUS_ONLINE.getCode());
    }


    /**
     * @description: 查询全部大修改资产
     * @author: HanHW
     * @date: 2023/12/7 18:33
     * @param: []
     * @return: java.util.Map<java.lang.Object, java.lang.Object>
     **/
    @Override
    public List<Asset> getOverhaulListV2() {
        QueryWrapper<SysOrg> query = Wrappers.query();
        query.eq("STATUS", StatusConst.OK);
        query.in("TYPE", Arrays.asList(OrgTypeConst.CENTER, OrgTypeConst.STATION));
        List<SysOrg> list = orgService.list(query);
        List<String> orgIds = list.stream().map(SysOrg::getId).collect(Collectors.toList());

        return assetMapper.getOverhaulList(orgIds);
    }

    /**
     * 查询组织下所有设备
     *
     * @param orgId 组织ID
     * @return
     */
    @Override
    public List<Asset> findListByOrgIdV2(String orgId) {
        return assetMapper.findListByOrgId(orgId);
    }

    /**
     * 按类型查询所有设备
     *
     * @param assetMode 设备类型
     * @return
     */
    @Override
    public List<Asset> findListByAssetModeV2(Integer assetMode) {
        return assetMapper.findListByAssetModeV2(assetMode);
    }

    /**
     * 资产按类型统计
     *
     * @param map
     * @return AssetStatisticsVo
     */
    @Override
    public List<AssetStatisticsVo> countModeV2(Map<String, Object> map) {

        this.getParamMap(map);

        return assetMapper.countModeV2(map);
    }

    private void getParamMap(Map<String, Object> map) {
        List<String> orgIds = ShiroUtil.getSubjectOrgIds();
        if (CollectionUtils.isEmpty(orgIds)) {
            throw new ResultException(ResultEnum.CANNOT_FIND);
        }

        Object orgId = map.get("orgId");
        if (Objects.isNull(orgId) || StringUtils.isEmpty(orgId.toString())) {
            map.put("orgIds", orgIds);
        } else {
            SysOrg org = orgService.getById(orgId.toString());
            if (Objects.isNull(org)) {
                throw new ResultException(ResultEnum.CANNOT_FIND);
            }
            if (OrgTypeConst.LINE == org.getType()) {
                Set<SysOrg> set = orgService.getChildrenById(orgId.toString());
                if (!CollectionUtils.isEmpty(set)) {
                    List<String> collect = set.stream().map(SysOrg::getId).collect(Collectors.toList());
                    List<String> collect1 = collect.stream().filter(orgIds::contains).collect(Collectors.toList());
                    map.put("orgIds", collect1);
                }
            }
        }
    }

    /**
     * 资产按型号统计
     *
     * @param map
     * @return AssetStatisticsVo
     */
    @Override
    public List<AssetStatisticsVo> countModelV2(Map<String, Object> map) {

        this.getParamMap(map);

        return assetMapper.countModelV2(map);
    }

    /**
     * ping测试
     *
     * @param ids
     * @return
     */
    @Override
    public List<AssetPingVo> assetPingV2(List<String> ids) {

        List<AssetPingVo> res = new ArrayList<>();
        Collection<Asset> assets = this.listByIds(ids);
        for (Asset asset : assets) {
            AssetPingVo assetPingVo = new AssetPingVo();
            assetPingVo.setName(asset.getName());

            RealTimePingStatusEnum result = null;
            try {
                result = collectAgent.realTimePing(asset);
            } catch (IOException e) {
                AppLogUtils.buildLogError(LogFunctionEnum.ASSET_MANAGE, asset.getIp(), e);
                assetPingVo.setRes("Ping发生异常请重试");
                res.add(assetPingVo);
            }
            if (RealTimePingStatusEnum.DOWN == result) {
                assetPingVo.setRes(IpPingStatusEnum.PING_NO.getMsg());
            } else if (RealTimePingStatusEnum.UP == result) {
                assetPingVo.setRes(IpPingStatusEnum.PING_YES.getMsg());
            } else {
                assetPingVo.setRes("未知");
            }
            res.add(assetPingVo);
        }
        return res;
    }

    /**
     * telnet测试
     *
     * @param reqList
     * @return
     */
    @Override
    public List<AssetTelnetVo> assetTelnetV2(List<AssetTelnet> reqList) {

        List<AssetTelnetVo> res = new ArrayList<>();
        for (AssetTelnet req : reqList) {
            Asset asset = this.getById(req.getId());
            String ip = asset.getIp();
            req.setPort(ToolUtil.chineseToEnglish(req.getPort()));
            req.setIp(ip);

            // port根据,拆分格式:端口号1,端口号2,区间1-区间2区间差小于等于30
            String[] portArr = req.getPort().split(",");

            // 测试结果
            StringBuilder success = new StringBuilder();
            StringBuilder fail = new StringBuilder();

            StringBuilder portStr = new StringBuilder();
            AssetTelnetVo telnetVo = new AssetTelnetVo();
            for (String portItem : portArr) {
                if (StringUtils.isEmpty(portItem)) {
                    portStr.append("端口").append(portItem).append("检测范围有误，端口为整数,范围为0-65535;");
                    continue;
                }

                portStr.append(portItem).append(":");
                try {
                    telnetVo = this.telnetPort(portItem, req, success, fail);
                    if (StringUtils.isEmpty(success.toString())) {
                        portStr.append("不可用;");
                    }
                    if (StringUtils.isEmpty(fail.toString())) {
                        portStr.append("可用;");
                    }
                } catch (AssetTelnetException ate) {
                    portStr.append(ate.getTelnetErrorMsg()).append(";");
                }
            }
            telnetVo.setRes(portStr.toString());
            telnetVo.setName(asset.getName());
            res.add(telnetVo);
        }
        return res;
    }

    @Override
    public AssetPerformanceDataVo queryPerformanceDataV2(String assetId) {
        AssetPerformanceDataVo vo = new AssetPerformanceDataVo();
        vo.setAssetId(assetId);
        Asset asset = getById(assetId);
        if (Objects.isNull(asset)) {
            return vo;
        }
        vo.setStatus(asset.getStatus());

        Object cpuUsedRe = cacheDataServ.queryAssetPerformanceData(asset, StatusInfoChangeTypeEnum.status_CPUState.getCode());
        Object memUsedRe = cacheDataServ.queryAssetPerformanceData(asset, StatusInfoChangeTypeEnum.status_memoryState.getCode());
        Object memTotal = cacheDataServ.queryAssetPerformanceData(asset, StatusInfoChangeTypeEnum.status_memory_total.getCode());
        Object memUsed = cacheDataServ.queryAssetPerformanceData(asset, StatusInfoChangeTypeEnum.status_memory_used.getCode());
        Object switchTotal = cacheDataServ.queryAssetPerformanceData(asset, StatusInfoChangeTypeEnum.status_switch_memory_total.getCode());
        Object switchUsed = cacheDataServ.queryAssetPerformanceData(asset, StatusInfoChangeTypeEnum.status_switch_memory_used.getCode());
        Object runTime = cacheDataServ.queryAssetPerformanceData(asset, StatusInfoChangeTypeEnum.status_run_time.getCode());
        Object deviationTime = cacheDataServ.queryAssetPerformanceData(asset, StatusInfoChangeTypeEnum.status_time_deviation.getCode());
        Object raidTotal = cacheDataServ.queryAssetPerformanceData(asset, StatusInfoChangeTypeEnum.status_raid_totalCapacity.getCode());
        Object raidFree = cacheDataServ.queryAssetPerformanceData(asset, StatusInfoChangeTypeEnum.status_raid_freeCapacity.getCode());


        if (Objects.nonNull(cpuUsedRe)) {
            BigDecimal cpu = new BigDecimal(cpuUsedRe.toString()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
            vo.setCpuUsedRate(cpu.doubleValue());
        }
        if (Objects.nonNull(memUsedRe)) {
            BigDecimal mem = new BigDecimal(memUsedRe.toString()).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP);
            vo.setMemUsedRate(mem.doubleValue());
        }

        if (Objects.nonNull(memTotal)) {
            String memTotalStr = UnitEnum.AutoScale(Long.parseLong(memTotal.toString()));
            vo.setMemoryTotal(memTotalStr);
        }
        if (Objects.nonNull(memUsed)) {
            String memUsedStr = UnitEnum.AutoScale(Long.parseLong(memUsed.toString()));
            vo.setMemoryUsed(memUsedStr);
        }
        if (Objects.nonNull(switchTotal)) {
            String switchTotalStr = UnitEnum.AutoScale(Long.parseLong(switchTotal.toString()));
            vo.setSwitchMemTotal(switchTotalStr);
        }
        if (Objects.nonNull(switchUsed)) {
            String switchUsedStr = UnitEnum.AutoScale(Long.parseLong(switchUsed.toString()));
            vo.setSwitchMemUsed(switchUsedStr);
        }
        if (Objects.nonNull(runTime)) {
            vo.setRunTime(Long.parseLong(runTime.toString()));
        }
        if (Objects.nonNull(deviationTime)) {
            vo.setDeviationTime(Long.parseLong(deviationTime.toString()));
        }

        if (Objects.nonNull(switchUsed) && Objects.nonNull(switchTotal)) {
            String rate = AppMathUtil.div(Long.parseLong(switchUsed.toString()), Long.parseLong(switchTotal.toString()), 2);
            vo.setSwitchMemUsedRate(Double.parseDouble(rate));
        }
        if (Objects.nonNull(raidTotal)) {
            vo.setRaidTotal(UnitEnum.getNetFileSizeDescription(Long.parseLong(raidTotal.toString())));
        }
        if (Objects.nonNull(raidFree)) {
            vo.setRaidFree(UnitEnum.getNetFileSizeDescription(Long.parseLong(raidFree.toString())));
        }
        if (Objects.nonNull(raidTotal) && Objects.nonNull(raidFree)) {
            long used = new BigDecimal(raidTotal.toString()).subtract(new BigDecimal(raidFree.toString())).longValue();
            vo.setRaidConfig(UnitEnum.getNetFileSizeDescription(used));
            //计算使用率
            if (used == 0) {
                vo.setRadeConfigRate(0d);
            } else {
                String rate = AppMathUtil.div(used, Long.parseLong(raidTotal.toString()), 2);
                vo.setRadeConfigRate(Double.parseDouble(rate));
            }
        }
        return vo;
    }

    @Override
    public ServerModuleVo serverModuleQueryV2(String assetId) {
        ServerModuleVo vo = new ServerModuleVo();
        Asset asset = getById(assetId);
        if (Objects.isNull(asset)) {
            return vo;
        }
        // 磁盘信息
        AssetDiskVo assetDiskVo = diskService.getAssetDiskMsg(assetId, UnitEnum.AUTO);
        //进程TOPO5
        List<DetailProcess> processCpuList = new ArrayList<>();
        List<DetailProcess> processMemList = new ArrayList<>();
        Map<String, Object> cpuTop5 = cacheDataServ.getProcessTop5V2(asset, "cpuTop5");
        Map<String, Object> memTop5 = cacheDataServ.getProcessTop5V2(asset, "memTop5");
        Set<String> keySet = cpuTop5.keySet();
        Set<String> memKeySet = memTop5.keySet();
        if (!keySet.isEmpty()) {
            for (String key : keySet) {
                Object s = cpuTop5.get(key);
                DetailProcess process = new DetailProcess();
                process.setName(key);
                process.setCpuUsedRate(s.toString());
                processCpuList.add(process);
            }
        }

        if (!memKeySet.isEmpty()) {
            for (String key : memKeySet) {
                Object s = memTop5.get(key);
                DetailProcess process = new DetailProcess();
                process.setName(key);
                process.setMemUsedRate(s.toString());
                processMemList.add(process);
            }
        }

        processCpuList.sort(Comparator.comparing(DetailProcess::getCpuUsedRate).reversed());
        processMemList.sort(Comparator.comparing(DetailProcess::getMemUsedRate).reversed());
        vo.setAssetDiskVo(assetDiskVo);
        vo.setProcessCpuTop(processCpuList);
        vo.setProcessMemTop(processMemList);

        return vo;
    }

    /**
     * 阈值管理 按条件查询资产ID
     *
     * @param manage
     * @return
     */
    @Override
    public List<AssetBaseInfoVo> getAssetIdListV2(ThresholdManage manage) {
        List<String> orgIds = manage.getOrgIds();
        List<String> orgIdList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orgIds)) {
            SysOrg one;
            for (String orgId : orgIds) {
                one = orgService.getById(orgId);
                if (one.getType() == OrgTypeConst.LINE) {
//                    Set<SysOrg> children = orgService.getChildrenById(orgId);
                    Map<String, Object> map = new HashMap<>();
                    map.put("pid", orgId);
                    map.put("userId", ShiroUtil.getSubject().getId());
                    List<SysOrg> levelOrg = orgService.getOrgsByUserId(map);
                    orgIdList.addAll(levelOrg.stream().map(SysOrg::getId).collect(Collectors.toList()));
                    continue;
                }
                orgIdList.add(orgId);
            }
            manage.setOrgIds(orgIdList);
        }

        return assetMapper.getAssetIdListV2(manage);
    }

    @Override
    public List<AssetStatusItmVo> queryBaseStatusItmV2(String assetId) {
        Asset asset = getById(assetId);
        ArrayList<AssetStatusItmVo> assetStatusItmVos = new ArrayList<>();
        if (Objects.isNull(asset)) {
            return assetStatusItmVos;
        }

        if (asset.isServer()) {
            List<CollectNetworkCard> realTimeData = netCardServ.getRealTimeData(assetId);
            if (!realTimeData.isEmpty()) {
                AssetStatusItmVo vo = new AssetStatusItmVo();
                QueryWrapper<AlarmInfo> query = Wrappers.query();
                query.eq("ASSET_ID", assetId);
                query.eq("ALARM_STATE", AlarmStateEnum.ALARM.getCode());
                query.eq("ALARM_CODE", StatusInfoChangeTypeEnum.event_net_state.getCode());
                int count = alarmInfoService.count(query);
                vo.setStatus(count == 0 ? 1 : -1);
                vo.setCode(AssetStatusItmVo.SERVER_NET_CARD);
                vo.setTitle("网卡信息列表");
                assetStatusItmVos.add(vo);
            }

            QueryWrapper<ThresholdProcess> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ASSET_ID", assetId);
            List<ThresholdProcess> processList = thresholdProcessService.list(queryWrapper);
            if (!processList.isEmpty()) {
                List<ThresholdProcess> collect = processList.stream().filter(item -> StatusEnum.NO.getCode() == item.getCollectStatus()).collect(Collectors.toList());
                AssetStatusItmVo vo = new AssetStatusItmVo();
                vo.setStatus(collect.isEmpty() ? 1 : -1);
                vo.setCode(AssetStatusItmVo.SERVER_PROCESS);
                vo.setTitle("进程列表");
                assetStatusItmVos.add(vo);
            }
        } else if (asset.isNetAsset()) {
            // 网络设备详情端口那个就修改成有端口告警了显示红色异常，没有了就显示绿色正常。
            AssetStatusItmVo vo = new AssetStatusItmVo();
            QueryWrapper<AlarmInfo> query = Wrappers.query();
            query.eq("ASSET_ID", assetId);
            query.eq("ALARM_STATE", AlarmStateEnum.ALARM.getCode());
            query.eq("ALARM_CODE", StatusInfoChangeTypeEnum.event_port_state.getCode());
            int count = alarmInfoService.count(query);
            vo.setStatus(count == 0 ? 1 : -1);

            vo.setCode(AssetStatusItmVo.SERVER_PORT);
            vo.setTitle("端口信息列表");
            assetStatusItmVos.add(vo);
        }

        List<CollectSensor> sensotList = sensorServ.getRealTimeData(assetId);
        List<CollectSensor> gaugeListTemp = sensotList.stream()
                .filter(item -> SensorTypeEnum.GAUGE.name().equals(item.getSensorType())).collect(Collectors.toList());

        List<CollectSensor> gaugeList = new ArrayList<>();
        for (CollectSensor collectSensor : gaugeListTemp) {
            String value = collectSensor.getValue();
            if (NumberUtil.isDouble(value) || NumberUtil.isNumber(value)) {
                if (Double.parseDouble(value) != 0) {
                    gaugeList.add(collectSensor);
                }
            }
        }
        if (gaugeList.isEmpty()) {
            QueryWrapper<CollectBhmTempInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ASSET_ID", assetId);
            queryWrapper.like("NAME", "Temp");
            List<CollectBhmTempInfo> list = collectBhmTempInfoService.list(queryWrapper);
            AssetStatusItmVo vo = new AssetStatusItmVo();
            vo.setStatus(1);
            for (CollectBhmTempInfo collectBhmTempInfo : list) {
                String health = collectBhmTempInfo.getHealth();
                if(!CollectBhmTempInfo.NORMAL_HEALTH.equals(health)){
                    vo.setStatus(-1);
                    break;
                }
            }
            vo.setCode(AssetStatusItmVo.SERVER_TEMP);
            vo.setTitle("温度健康状态");
            if(!list.isEmpty()){
                assetStatusItmVos.add(vo);
            }
        }else{
            AssetStatusItmVo vo = new AssetStatusItmVo();
            vo.setStatus(1);
            for (CollectSensor collectSensor : gaugeList) {
                if ("2".equals(collectSensor.getStatus())) {
                    vo.setStatus(-1);
                    break;
                }
            }
            vo.setCode(AssetStatusItmVo.SERVER_TEMP);
            vo.setTitle("温度信息列表");
            assetStatusItmVos.add(vo);
        }

        List<CollectSensor> fanList = sensotList.stream()
                .filter(item -> SensorTypeEnum.FAN.name().equals(item.getSensorType())).collect(Collectors.toList());

        if (fanList.isEmpty()) {
            QueryWrapper<CollectBhmFanInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ASSET_ID", assetId);
            List<CollectBhmFanInfo> list = collectBhmFanInfoService.list(queryWrapper);
            AssetStatusItmVo vo = new AssetStatusItmVo();
            vo.setStatus(1);
            for (CollectBhmFanInfo fanInfo : list) {
                String health = fanInfo.getHealth();
                if(!CollectBhmFanInfo.NORMAL_HEALTH.equals(health)){
                    vo.setStatus(-1);
                    break;
                }
            }
            vo.setCode(AssetStatusItmVo.SERVER_FAN);
            vo.setTitle("风扇健康状态");

            if(!list.isEmpty()){
                assetStatusItmVos.add(vo);
            }
        }else{
            AssetStatusItmVo vo = new AssetStatusItmVo();
            vo.setStatus(1);
            for (CollectSensor collectSensor : fanList) {
                if ("2".equals(collectSensor.getStatus())) {
                    vo.setStatus(-1);
                    break;
                }
            }
            vo.setCode(AssetStatusItmVo.SERVER_FAN);
            vo.setTitle("风扇信息列表");
            assetStatusItmVos.add(vo);
        }

        List<CollectSensor> powerList = sensotList.stream()
                .filter(item -> SensorTypeEnum.POWER.name().equals(item.getSensorType())).collect(Collectors.toList());

        if (powerList.isEmpty()) {
            QueryWrapper<CollectBhmPowerInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ASSET_ID", assetId);
            List<CollectBhmPowerInfo> list = collectBhmPowerInfoService.list(queryWrapper);
            AssetStatusItmVo vo = new AssetStatusItmVo();
            vo.setStatus(1);
            for (CollectBhmPowerInfo collectBhmPowerInfo : list) {
                String health = collectBhmPowerInfo.getHealth();
                if(!CollectBhmPowerInfo.NORMAL_HEALTH.equals(health)){
                    vo.setStatus(-1);
                    break;
                }
            }
            vo.setCode(AssetStatusItmVo.SERVER_POWER);
            vo.setTitle("电源健康状态");

            if(!list.isEmpty()){
                assetStatusItmVos.add(vo);
            }
        }else{
            AssetStatusItmVo vo = new AssetStatusItmVo();
            vo.setStatus(1);
            for (CollectSensor collectSensor : powerList) {
                if ("2".equals(collectSensor.getStatus())) {
                    vo.setStatus(-1);
                    break;
                }
            }
            vo.setCode(AssetStatusItmVo.SERVER_POWER);
            vo.setTitle("电源信息列表");
            assetStatusItmVos.add(vo);
        }


        return assetStatusItmVos;
    }

    @Override
    public AssetStatusDetailVo getAssetStatusDetailV2(String assetId, String code) {
        AssetStatusDetailVo vo = new AssetStatusDetailVo();
        if (AssetStatusItmVo.SERVER_NET_CARD.equals(code)) {
            List<CollectNetworkCard> realTimeData = netCardServ.getRealTimeData(assetId);

            List<CollectNetworkCard> normalList = new ArrayList<CollectNetworkCard>();
            List<CollectNetworkCard> errorList = new ArrayList<CollectNetworkCard>();
            for (CollectNetworkCard realTimeDatum : realTimeData) {
                realTimeDatum.setStatusStr(CollectNetCardStatus.getMsg(realTimeDatum.getStatus()));
                if (CollectNetCardStatus.UP.getCode().equals(realTimeDatum.getStatus())) {
                    normalList.add(realTimeDatum);
                } else {
                    errorList.add(realTimeDatum);
                }
            }

            List<JSONObject> titleList = WebTitleUtils.getWebTitleList(CollectNetworkCard.class);

            errorList.addAll(normalList);
            vo.setTitleList(titleList);
            vo.setDataList(errorList);
        } else if (AssetStatusItmVo.SERVER_PROCESS.equals(code)) {
            QueryWrapper<ThresholdProcess> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ASSET_ID", assetId);
            List<ThresholdProcess> processList = thresholdProcessService.list(queryWrapper);

            List<ThresholdProcess> normalList = new ArrayList<ThresholdProcess>();
            List<ThresholdProcess> errorList = new ArrayList<ThresholdProcess>();
            for (ThresholdProcess thresholdProcess : processList) {
                thresholdProcess.setCollectStatusStr(StatusEnum.getMsgByCode(thresholdProcess.getCollectStatus()));
                if (StatusEnum.OK.getCode().equals(thresholdProcess.getCollectStatus())) {
                    normalList.add(thresholdProcess);
                } else {
                    errorList.add(thresholdProcess);
                }
            }

            List<JSONObject> webTitleList = WebTitleUtils.getWebTitleList(ThresholdProcess.class);

            errorList.addAll(normalList);
            vo.setTitleList(webTitleList);
            vo.setDataList(errorList);

        } else if (AssetStatusItmVo.SERVER_TEMP.equals(code)) {
            // 温度
            List<CollectSensor> sensotList = sensorServ.getRealTimeData(assetId);
            List<CollectSensor> gaugeListTemp = sensotList.stream()
                    .filter(item -> SensorTypeEnum.GAUGE.name().equals(item.getSensorType())).collect(Collectors.toList());
            List<CollectSensor> gaugeList = new ArrayList<>();
            for (CollectSensor collectSensor : gaugeListTemp) {
                String value = collectSensor.getValue();
                if (NumberUtil.isDouble(value) || NumberUtil.isNumber(value)) {
                    if (Double.parseDouble(value) != 0) {
                        gaugeList.add(collectSensor);
                    }
                }
            }

            if(gaugeList.isEmpty()){
                QueryWrapper<CollectBhmTempInfo> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("ASSET_ID", assetId);
                queryWrapper.like("NAME", "Temp");
                List<CollectBhmTempInfo> list = collectBhmTempInfoService.list(queryWrapper);

                if(Objects.nonNull(list) && !list.isEmpty()){
                    List<JSONObject> titleList = WebTitleUtils.getWebTitleList(CollectBhmTempInfo.class);

                    vo.setTitleList(titleList);
                    vo.setDataList(list);
                }
            }else{
                JSONObject name = new JSONObject();
                name.put("property", "serialNumberName");
                name.put("title", "序列号");
                JSONObject titleValue = new JSONObject();
                titleValue.put("property", "value");
                titleValue.put("title", "温度");
                JSONObject titleStatus = new JSONObject();
                titleStatus.put("property", "statusStr");
                titleStatus.put("title", "状态");

                vo.setTitleList(Arrays.asList(name, titleValue, titleStatus));
                vo.setDataList(gaugeList);
            }

        } else if (AssetStatusItmVo.SERVER_FAN.equals(code)) {
            // 风扇
            List<CollectSensor> sensotList = sensorServ.getRealTimeData(assetId);
            List<CollectSensor> fanList = sensotList.stream()
                    .filter(item -> SensorTypeEnum.FAN.name().equals(item.getSensorType())).collect(Collectors.toList());

            if(fanList.isEmpty()){
                QueryWrapper<CollectBhmFanInfo> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("ASSET_ID", assetId);
                List<CollectBhmFanInfo> list = collectBhmFanInfoService.list(queryWrapper);
                if(Objects.nonNull(list) && !list.isEmpty()){
                    List<JSONObject> titleList = WebTitleUtils.getWebTitleList(CollectBhmFanInfo.class);

                    vo.setTitleList(titleList);
                    vo.setDataList(list);
                }
            }else{
                JSONObject name = new JSONObject();
                name.put("property", "serialNumberName");
                name.put("title", "序列号");
                JSONObject titleValue = new JSONObject();
                titleValue.put("property", "value");
                titleValue.put("title", "转速");
                JSONObject titleStatus = new JSONObject();
                titleStatus.put("property", "statusStr");
                titleStatus.put("title", "状态");

                vo.setTitleList(Arrays.asList(name, titleValue, titleStatus));
                vo.setDataList(fanList);
            }

        } else if (AssetStatusItmVo.SERVER_POWER.equals(code)) {
            // 电源
            List<CollectSensor> sensotList = sensorServ.getRealTimeData(assetId);
            List<CollectSensor> powerList = sensotList.stream()
                    .filter(item -> SensorTypeEnum.POWER.name().equals(item.getSensorType())).collect(Collectors.toList());

            if(powerList.isEmpty()){
                QueryWrapper<CollectBhmPowerInfo> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("ASSET_ID", assetId);
                List<CollectBhmPowerInfo> list = collectBhmPowerInfoService.list(queryWrapper);

                if(Objects.nonNull(list) && !list.isEmpty()){
                    List<JSONObject> fieldJson = WebTitleUtils.getWebTitleList(CollectBhmPowerInfo.class);

                    vo.setTitleList(fieldJson);
                    vo.setDataList(list);
                }
            }else{
                JSONObject name = new JSONObject();
                name.put("property", "serialNumberName");
                name.put("title", "序列号");
                JSONObject titleValue = new JSONObject();
                titleValue.put("property", "value");
                titleValue.put("title", "电压");
                JSONObject titleStatus = new JSONObject();
                titleStatus.put("property", "statusStr");
                titleStatus.put("title", "状态");

                vo.setTitleList(Arrays.asList(name, titleValue, titleStatus));
                vo.setDataList(powerList);
            }
        } else if (AssetStatusItmVo.SERVER_PORT.equals(code)) {
            //端口
            List<AssetPortVo> ports = topoAssetPortService.selectPortListByAsset(assetId);
            List<AssetPortVo> collect = ports.stream().map(p -> {
                if (p.getStatus() == 0) {
                    p.setPortName("闲");
                } else if (p.getStatus() == 1) {
                    p.setPortName("通");
                } else if (p.getStatus() == 2) {
                    p.setPortName("断");
                }
                return p;
            }).collect(Collectors.toList());
            JSONObject name = new JSONObject();
            name.put("property", "portIndex");
            name.put("title", "端口名称");
            JSONObject titleStatus = new JSONObject();
            titleStatus.put("property", "portName");//0未使用 1正常 2异常
            titleStatus.put("title", "状态");

            vo.setTitleList(Arrays.asList(name, titleStatus));
            vo.setDataList(collect);
        }
        return vo;
    }

    @Override
    public List<AssetLifeLineVo> getLifeLineV2(String assetId) {
        //查询设备所有的硬件更换记录
        List<AssetLifeLineVo> fixList = fixServ.getLifeLineV2(assetId);
        //查询设备所有的故障记录
        List<AssetLifeLineVo> recordList = brokenRecordServ.getLifeLineV2(assetId);
        fixList.addAll(recordList);
        //设备的上下架时间
        Asset asset = assetMapper.selectById(assetId);
        if (Objects.isNull(asset)) {
            throw new ResultException(ResultEnum.PARAM_ERROR, "资产不存在");
        }
        Date onlineTime = asset.getOnlineTime();
        Date downLineTime = asset.getDownlineTime();
        if (Objects.nonNull(onlineTime)) {
            AssetLifeLineVo vo = new AssetLifeLineVo();
            vo.setLinkId(assetId);
            vo.setTitle("设备上架");
            vo.setType(AssetLifeLineVo.LifeLineType.UP.getCode());
            vo.setCreateTime(onlineTime);
            fixList.add(vo);
        }
        if (Objects.nonNull(downLineTime)) {
            AssetLifeLineVo vo = new AssetLifeLineVo();
            vo.setLinkId(assetId);
            vo.setTitle("设备下架");
            vo.setType(AssetLifeLineVo.LifeLineType.DOWN.getCode());
            vo.setCreateTime(downLineTime);
            fixList.add(vo);
        }


        if (!fixList.isEmpty()) {
            List<AssetLifeLineVo> collect = fixList.stream().sorted(Comparator.comparing(item -> item.getCreateTime())).collect(Collectors.toList());
            return collect;
        }

        return fixList;
    }

    @Override
    public void saveDevice(List<Asset> assets) {
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ASSET_MODE", "30");
        List<String> assetIds = assetMapper.selectList(queryWrapper).stream().map(Asset::getId).collect(Collectors.toList());
        for (Asset asset : assets) {
            if (!assetIds.isEmpty()) {
                assetIds.remove(asset.getId());
            }
            this.saveOrUpdate(asset);
            AssetAttach assetAttach = new AssetAttach();
            assetAttach.setAssetId(asset.getId());
            assetAttach.setOrgId(asset.getOrgId());
            assetAttach.setRoomId(asset.getRoomId());
            assetAttachService.saveOrUpdate(assetAttach);
        }
        //删除这些Asset及对应信息
        if (!assetIds.isEmpty()) {
            assetMapper.deleteBatchIds(assetIds);
            assetAttachService.removeByIds(assetIds);
        }

    }

}
