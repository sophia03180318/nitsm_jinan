package com.jcca.web2.service.notify;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.*;
import com.jcca.web.asset.service.*;
import com.jcca.web.asset.service.bean.AddAssetException;
import com.jcca.web.broken.service.BrokenRecordService;
import com.jcca.web.collect.entity.*;
import com.jcca.web.collect.service.*;
import com.jcca.web.common.constants.OutConst;
import com.jcca.web.db.service.ManageDbService;
import com.jcca.web.event.entity.AlarmEventRel;
import com.jcca.web.event.service.AlarmEventRelService;
import com.jcca.web.event.service.AlarmEventService;
import com.jcca.web.ip.service.IpInfoService;
import com.jcca.web.xunjian.entity.XunjianDetailV2;
import com.jcca.web.xunjian.service.XunjianDetailV2Service;
import com.jcca.web2.entity.ThresholdManage;
import com.jcca.web2.service.InspectRecordService;
import com.jcca.web2.service.ThresholdManageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HanHW
 * @description 资产变动删除资产
 * @className NotifyDelAssetImpl
 * @date 2023/12/6 9:29
 * @since 2.1.0.0
 */
@Service
public class NotifyDelAssetImpl {

    @Resource
    private AssetService assetService;
    @Resource
    private ManageDbService manageDbService;
    @Resource
    private AlarmInfoService alarmService;
    @Resource
    private BrokenRecordService brokenRecordServ;
    @Resource
    private AssetHardwareFixService fixServ;
    @Resource
    private XunjianDetailV2Service xunjianV2DetailServ;
    @Resource
    private AlarmEventRelService relServ;
    @Resource
    private AlarmEventService eventServ;
    @Resource
    private AssetAttachService assetAttachService;
    @Resource
    private CollectAixAdapterService aixAdapterServ;
    @Resource
    private CollectConnectService connectServ;
    @Resource
    private CollectDBService dbServ;
    @Resource
    private CollectDBfileService collectDBfile;
    @Resource
    private CollectDsService dsService;
    @Resource
    private CollectPcbService pcbService;
    @Resource
    private CollectPortService collectPortServ;
    @Resource
    private CollectProcessService processServ;
    @Resource
    private CollectRaidService raidServ;
    @Resource
    private CollectSensorService sensorServ;
    @Resource
    private CollectSystemTimeService timeServ;
    @Resource
    private CollectTablespaceService tableSpaceServ;
    @Resource
    private CollectVlanService vlanServ;
    @Resource
    private ThresholdAssetService thresholdAssetService;
    @Resource
    private ThresholdProcessService thresholdProcessService;
    @Resource
    private CollectRouteService routeServ;
    @Resource
    private IpInfoService ipInfoService;
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
    @Resource
    private ThresholdManageService thresholdManageService;
    @Resource
    private InspectRecordService inspectRecordService;
    @Resource
    private AssetAppServerService appServerService;
    @Resource
    private CollectHardwareService collectHardwareService;

    /**
     * OutConst
     * 0新增，1删除，2修改
     *
     * @param asset 变动的资产
     * @param state 0新增，1删除，2修改
     */
    public void assetChange(Asset asset, Integer state) throws AddAssetException {
        if (OutConst.DEL_ASSET.intValue() != state) {
            return;
        }
        String assetId = asset.getId();

        //资产附属数据
        QueryWrapper<AssetAttach> attachQuery = new QueryWrapper<AssetAttach>();
        attachQuery.eq("ASSET_ID", assetId);
        assetAttachService.remove(attachQuery);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产附属数据成功");

        //删除采集数据
        delCollectData(assetId);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产采集数据成功");

        QueryWrapper<XunjianDetailV2> queryV2 = new QueryWrapper<XunjianDetailV2>();
        queryV2.eq("ASSET_ID", assetId);
        xunjianV2DetailServ.remove(queryV2);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产巡检V2数据成功");

        // 删除资产 机房 机柜关系
        assetAttachService.removeById(assetId);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产 机房 机柜关系成功");

        // 删除资产阈值
        thresholdAssetService.removeById(assetId);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产阈值成功");

        // 删除资产阈值V2
        QueryWrapper<ThresholdManage> thresholdManage = Wrappers.query();
        thresholdManage.eq("ASSET_ID", assetId);
        thresholdManageService.remove(thresholdManage);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产阈值V2成功");

        // 删除资产上对应的数据库
        manageDbService.removeDBById(null, assetId);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产上对应的数据库成功");

        // 删除发现路由
        QueryWrapper<CollectRoute> queryWrapper = new QueryWrapper<CollectRoute>();
        queryWrapper.eq("ASSET_ID", assetId);
        routeServ.remove(queryWrapper);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产发现路由成功");

        // 删除相关告警
        QueryWrapper<AlarmInfo> alarmInfoQuery = new QueryWrapper<AlarmInfo>();
        alarmInfoQuery.eq("ASSET_ID", assetId);
        List<AlarmInfo> list = alarmService.list(alarmInfoQuery);
        for (AlarmInfo alarmInfo : list) {
            QueryWrapper<AlarmEventRel> relQuery = new QueryWrapper<AlarmEventRel>();
            relQuery.eq("ALARM_ID", alarmInfo.getId());
            relServ.remove(relQuery);
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产中的告警事件关联成功");

        alarmService.remove(alarmInfoQuery);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产中的告警成功");

        // 删除相关事件
        eventServ.removeByAssetId(assetId);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产事件成功");

        // 删除相关进程
        thresholdProcessService.removeByAssetId(assetId);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产进程配置成功");

        if (StrUtil.isNotEmpty(asset.getIp())) {
            // 恢复资产占用的IP
            ipInfoService.liberateIp(asset.getIp());
            AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "恢复资产占用的IP1成功");
        }
        if (StrUtil.isNotEmpty(asset.getIp2())) {
            // 恢复资产占用的IP
            ipInfoService.liberateIp(asset.getIp2());
            AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "恢复资产占用的IP2成功");
        }

        //删除维护计划
        brokenRecordServ.removeAssetLog(assetId);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产维护计划成功");

        //删除硬件维护记录
        QueryWrapper<AssetHardwareFix> query = new QueryWrapper<AssetHardwareFix>();
        query.eq("ASSET_ID", assetId);
        fixServ.remove(query);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除资产硬件维护记录成功");

        // 删除巡检记录
        inspectRecordService.deleteInspectByAssetId(assetId);
        AppLogUtils.buildLogInfo(LogFunctionEnum.ASSET_CHANGE, assetId, "删除巡检记录成功");

        // 如果是应用服务器
        if (asset.getServiceType() != null) {
            appServerService.deleteServerPort(assetId, null);
        }

        // ============================================所有要删除数据应该在删除资产前操作==================================
        // 删除资产
        assetService.removeById(assetId);
    }

    private void delCollectData(String assetId) {
        QueryWrapper<CollectCpu> cpuData = new QueryWrapper<CollectCpu>();
        cpuData.eq("ASSET_ID", assetId);
        cpuService.remove(cpuData);
        QueryWrapper<CollectDisk> diskData = new QueryWrapper<CollectDisk>();
        diskData.eq("ASSET_ID", assetId);
        diskService.remove(diskData);
        QueryWrapper<CollectAIXAdapter> aixAdapterData = new QueryWrapper<CollectAIXAdapter>();
        aixAdapterData.eq("ASSET_ID", assetId);
        aixAdapterServ.remove(aixAdapterData);
        QueryWrapper<CollectConnect> connectAdapterData = new QueryWrapper<CollectConnect>();
        connectAdapterData.eq("ASSET_ID", assetId);
        connectServ.remove(connectAdapterData);
        QueryWrapper<CollectDB> dbReq = new QueryWrapper<CollectDB>();
        dbReq.eq("ASSET_ID", assetId);
        dbServ.remove(dbReq);
        QueryWrapper<CollectDBfile> dbFileReq = new QueryWrapper<CollectDBfile>();
        dbFileReq.eq("ASSET_ID", assetId);
        collectDBfile.remove(dbFileReq);
        QueryWrapper<CollectDS> dsQuery = new QueryWrapper<CollectDS>();
        dsQuery.eq("ASSET_ID", assetId);
        dsService.remove(dsQuery);
        QueryWrapper<CollectInterfaces> interfacesReq = new QueryWrapper<CollectInterfaces>();
        interfacesReq.eq("ASSET_ID", assetId);
        interfacesServ.remove(interfacesReq);
        QueryWrapper<CollectMemory> memoryReq = new QueryWrapper<CollectMemory>();
        memoryReq.eq("ASSET_ID", assetId);
        memoryServ.remove(memoryReq);
        QueryWrapper<CollectNetworkCard> netCardReq = new QueryWrapper<CollectNetworkCard>();
        netCardReq.eq("ASSET_ID", assetId);
        netCardServ.remove(netCardReq);
        QueryWrapper<CollectPcb> pcbReq = new QueryWrapper<CollectPcb>();
        pcbReq.eq("ASSET_ID", assetId);
        pcbService.remove(pcbReq);
        QueryWrapper<CollectPort> portReq = new QueryWrapper<CollectPort>();
        portReq.eq("ASSET_ID", assetId);
        collectPortServ.remove(portReq);
        QueryWrapper<CollectProcess> processReq = new QueryWrapper<CollectProcess>();
        processReq.eq("ASSET_ID", assetId);
        processServ.remove(processReq);
        QueryWrapper<CollectRaid> raid = new QueryWrapper<CollectRaid>();
        raid.eq("ASSET_ID", assetId);
        raidServ.remove(raid);
        QueryWrapper<CollectSensor> sensotReq = new QueryWrapper<CollectSensor>();
        sensotReq.eq("ASSET_ID", assetId);
        sensorServ.remove(sensotReq);
        QueryWrapper<CollectSystemTime> timeReq = new QueryWrapper<CollectSystemTime>();
        timeReq.eq("ASSET_ID", assetId);
        timeServ.remove(timeReq);
        QueryWrapper<CollectTablespace> tablespaceReq = new QueryWrapper<CollectTablespace>();
        tablespaceReq.eq("ASSET_ID", assetId);
        tableSpaceServ.remove(tablespaceReq);
        QueryWrapper<CollectVlan> vlanReq = new QueryWrapper<CollectVlan>();
        vlanReq.eq("ASSET_ID", assetId);
        vlanServ.remove(vlanReq);
        // 删除资产硬件信息
        QueryWrapper<CollectHardware> query = Wrappers.query();
        query.eq("ASSET_ID", assetId);
        collectHardwareService.remove(query);
    }
}
