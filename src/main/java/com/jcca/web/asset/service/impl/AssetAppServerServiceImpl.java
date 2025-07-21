package com.jcca.web.asset.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.dao.AssetAppServerMapper;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAppServer;
import com.jcca.web.asset.entity.CollectCpuLoad;
import com.jcca.web.asset.service.AssetAppServerService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.CollectCpuLoadService;
import com.jcca.web.asset.vo.AssetAppServerVo;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: hhw
 * @description: AssetAppServerServiceImpl 主要是用来
 * @date: 2025-07-03  16:54
 * @since: 2.0.15.0
 */
@Service
public class AssetAppServerServiceImpl extends ServiceImpl<AssetAppServerMapper, AssetAppServer> implements AssetAppServerService {

    @Resource
    private AssetAppServerMapper assetAppServerMapper;
    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private CollectCpuLoadService collectCpuLoadService;
    @Resource
    private AssetService assetService;


    @Override
    public List<AssetAppServerVo> getAppServerList() {

        List<AssetAppServerVo> list = assetAppServerMapper.getAppServerList();
        List<AssetAppServerVo> resList = this.setAppServerInfo(list);
        return resList;
    }

    private List<AssetAppServerVo> setAppServerInfo(List<AssetAppServerVo> list) {
        List<AssetAppServerVo> resList = new ArrayList<>();
        Map<String, List<AssetAppServerVo>> assetMap = list.stream().collect(Collectors.groupingBy(AssetAppServerVo::getAssetId));
        assetMap.forEach((assetId, assetAppServers) -> {
            AssetAppServerVo vo = new AssetAppServerVo();
            vo.setId(assetId);
            vo.setAssetId(assetId);
            vo.setItemName(assetAppServers.get(0).getItemName());
            vo.setAlarmState(AlarmStateEnum.RECOVER.getCode());

            this.setAlarmState(assetId, vo);
            this.setCpuLoad(assetId, vo);

            List<AssetAppServerVo> children = new ArrayList<>();
            if (!StringUtils.isEmpty(assetAppServers.get(0).getServerPort())) {
                Map<String, List<AssetAppServerVo>> collect = assetAppServers.stream().collect(Collectors.groupingBy(AssetAppServerVo::getServerPort));
                collect.forEach((serverPort, servers) -> {
                    AssetAppServerVo voo = new AssetAppServerVo();
                    voo.setId(serverPort);
                    voo.setAssetId(assetId);
                    voo.setItemName(serverPort);
                    List<AssetAppServerVo> collect1 = servers.stream().filter(server -> server.getLinkStatus() != null)
                            .sorted(Comparator.comparing(AssetAppServerVo::getLinkStatus)).collect(Collectors.toList());
                    voo.setChildren(collect1);
                    children.add(voo);
                });
            }
            children.sort(Comparator.comparing(o -> {
                try {
                    return Integer.parseInt(o.getItemName());
                } catch (NumberFormatException e) {
                    return Integer.MAX_VALUE; // 将非数字字符串排在最后
                }
            }));
            vo.setChildren(children);
            resList.add(vo);
        });

        resList.sort(Comparator.comparing(AssetAppServerVo::getItemName));
        return resList;
    }

    private void setCpuLoad(String assetId, AssetAppServerVo vo) {
        Asset asset = assetService.getById(assetId);
        if (asset == null || asset.getServiceType() == null || asset.getServiceType() != 1) {
            return;
        }
        CollectCpuLoad collectCpuLoad = collectCpuLoadService.getLastRecordByAssetId(assetId);
        if (collectCpuLoad != null) {
            vo.setLoadOne(collectCpuLoad.getLoadOne());
            vo.setLoadFive(collectCpuLoad.getLoadFive());
            vo.setLoadFifteen(collectCpuLoad.getLoadFifteen());
            vo.setCollectTime(DateUtil.formatDateTime(collectCpuLoad.getCollectTime()));
        }
    }

    private void setAlarmState(String assetId, AssetAppServerVo vo) {
//        QueryWrapper<AlarmInfo> query = Wrappers.query();
//        query.eq("asset_id", assetId);
//        query.eq("alarm_state", AlarmStateEnum.ALARM.getCode());
//        query.eq("status", AlarmStatusEnum.UNCONFIRM.getCode());
//        query.like("alarm_code", EventUniqueCode.ASSET_APP_LINK);
//        int count = alarmInfoService.count(query);
//        if (count > 0) {
//            vo.setAlarmState(AlarmStateEnum.ALARM.getCode());
//        }

        QueryWrapper<AssetAppServer> query = Wrappers.query();
        query.eq("ASSET_ID", assetId);
        query.eq("LINK_STATUS", 0);
        int count = this.count(query);
        if (count > 0) {
            vo.setAlarmState(AlarmStateEnum.ALARM.getCode());
        }
    }

    @Override
    public List<AssetAppServer> findByAssetIdAndServerPort(String assetId, String serverPort) {
        return assetAppServerMapper.findByAssetIdAndServerPort(assetId, serverPort);
    }

    @Override
    public List<AssetAppServer> findByAssetIdNullLink(String assetId) {
        return assetAppServerMapper.findByAssetIdNullLink(assetId);
    }

    @Override
    public List<AssetAppServerVo> getAppServerListByAssetId(String assetId) {
        List<AssetAppServerVo> resList = new ArrayList<>();
        List<AssetAppServerVo> list = assetAppServerMapper.getAppServerListByAssetId(assetId);
        Map<String, List<AssetAppServerVo>> collect = list.stream().collect(Collectors.groupingBy(AssetAppServerVo::getServerPort));
        collect.forEach((serverPort, servers) -> {
            AssetAppServerVo vo = new AssetAppServerVo();
            vo.setId(serverPort);
            vo.setAssetId(assetId);
            vo.setItemName(serverPort);
            List<AssetAppServerVo> collect1 = servers.stream().filter(server -> server.getLinkStatus() != null)
                    .sorted(Comparator.comparing(AssetAppServerVo::getLinkStatus)).collect(Collectors.toList());
            vo.setChildren(collect1);
            resList.add(vo);
        });

        resList.sort(Comparator.comparing(o -> {
            try {
                return Integer.parseInt(o.getItemName());
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE; // 将非数字字符串排在最后
            }
        }));
        return resList;
    }

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public void deleteServerPort(String assetId, Integer serverPort) {
        if (serverPort != null) {
            List<AssetAppServer> list = this.findByAssetIdAndServerPort2(assetId, serverPort);
            for (AssetAppServer assetAppServer : list) {
                QueryWrapper<AlarmInfo> query = Wrappers.query();
                query.eq("ASSET_ID", assetId);
                query.eq("ALARM_CODE", StatusInfoChangeTypeEnum.event_appServer_link.getCode());
                query.like("ALARM_FLAG", assetId + "_" + assetAppServer.getServerPort() + "_");
                List<AlarmInfo> infos = alarmInfoService.list(query);
                for (AlarmInfo info : infos) {
                    alarmInfoService.delAlarm(info.getId());
                }
            }
        }

        UpdateWrapper<AssetAppServer> update = Wrappers.update();
        update.eq("ASSET_ID", assetId);
        if (serverPort != null) {
            update.eq("SERVER_PORT", serverPort);
        }
        assetAppServerMapper.delete(update);

        // 删除缓存
        Asset asset = assetService.getById(assetId);
        String redisKey = asset.getIp() + ":" + assetId + ":" + StatusInfoChangeTypeEnum.event_appServer_link.getCode() + ":" + serverPort;
        String mapKey = serverPort + "";
        eventInfoChangeManagerService.delStateValue(redisKey, mapKey);
        eventInfoChangeManagerService.delRedisKey(redisKey);
    }

    @Override
    public List<AssetAppServer> findByAssetIdAndServerPort2(String assetId, Integer serverPort) {
        return assetAppServerMapper.findByAssetIdAndServerPort2(assetId, serverPort);
    }

    @Override
    public List<AssetAppServerVo> getAppServerInfo(String assetId) {
        List<AssetAppServerVo> voList = assetAppServerMapper.getAppServerInfo(assetId);
        List<AssetAppServerVo> resList = this.setAppServerInfo(voList);
        return resList;
    }

    @Override
    public void setLinkStatus(String alarmCode, Integer linkStatus) {
        try {
            String[] split = alarmCode.split("_");
            UpdateWrapper<AssetAppServer> update = Wrappers.update();
            update.eq("ASSET_ID", split[1]);
            update.eq("SERVER_PORT", split[2]);
            update.eq("LINK_IP", split[3]);
            update.set("LINK_STATUS", linkStatus);
            this.update(update);
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.APP_SERVER_LINK, "设置应用服务器连接失败", e);
        }
    }

    @Override
    public AssetAppServer findOneLinkData(String assetId, Integer serverPort, String linkIp) {
        return assetAppServerMapper.findOneLinkData(assetId, serverPort, linkIp);
    }

    @Override
    public void deleteLinkData(String alarmFlag) {
        try {
            String[] split = alarmFlag.split("_");
            UpdateWrapper<AssetAppServer> update = Wrappers.update();
            update.eq("ASSET_ID", split[1]);
            update.eq("SERVER_PORT", split[2]);
            update.eq("LINK_IP", split[3]);
            this.remove(update);
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.APP_SERVER_LINK, "删除应用服务器连接失败", e);
        }
    }
}
