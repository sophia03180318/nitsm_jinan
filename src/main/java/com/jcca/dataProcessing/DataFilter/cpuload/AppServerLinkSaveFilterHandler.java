package com.jcca.dataProcessing.DataFilter.cpuload;

import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectCpuLoadBean;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetAppServer;
import com.jcca.web.asset.service.AssetAppServerService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author: hhw
 * @description: AppServerLinkSaveFilterHandler 主要是用来处理应用服务器连接告警
 * @date: 2025-07-17  16:15
 * @since: 2.1.8.0
 */
@Component("appServerLinkSaveFilterHandler")
public class AppServerLinkSaveFilterHandler extends IFilterHandler<CollectCpuLoadBean> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private AssetAppServerService assetAppServerService;
    @Resource
    private AssetService assetService;
    @Resource
    private RedisService redisService;

    /**
     * 处理方法
     *
     * @param info
     * @return 返回值true表示可以进入下一层处理，放回false
     * 表示整个处理结束，不会进入下层处理，不会保存缓存
     */
    @Override
    public boolean handler(CollectCpuLoadBean info) throws Exception {
        Map<String, Set<String>> connectInfoMap = info.getConnectInfoMap();
        if (connectInfoMap == null) {
            return false;
        }
        String assetId = info.getAssetId();
        Asset asset = assetService.getById(assetId);
        Set<Map.Entry<String, Set<String>>> entries = connectInfoMap.entrySet();
        for (Map.Entry<String, Set<String>> entry : entries) {
            String redisKey = info.getAssetIp() + ":" + assetId + ":" + StatusInfoChangeTypeEnum.event_appServer_link.getCode() + ":" + entry.getKey();
            String mapKey = entry.getKey();
            Set<String> newIpSet = entry.getValue();
            StringBuilder sb = new StringBuilder();
            for (String ip : newIpSet) {
                sb.append(ip).append(",");
            }
            String redisValue = sb.substring(0, sb.lastIndexOf(","));
            int linkStatus = 1;
            String oldKey = info.getAssetIp() + ":" + assetId + ":temp_app_link:port:" + entry.getKey();
            Object oldValue = redisService.get(oldKey);
            if (Objects.isNull(oldValue)) {
                redisService.set(oldKey, redisValue);
                this.saveLinkData(info, entry, asset);
                this.handleEvent(info, newIpSet, redisKey, mapKey, linkStatus, redisValue);
                continue;
            }

            String[] split = oldValue.toString().split(",");
            Set<String> oldIpSet = new HashSet<>(Arrays.asList(split));
            Set<String> difference = this.getDifference(oldIpSet, newIpSet);
            if (!difference.isEmpty()) {
                linkStatus = 0;
                this.updateLinkData(info, difference, linkStatus, mapKey, redisKey, redisValue, asset);
            }

            Set<String> intersection = this.getDifference(newIpSet, oldIpSet);
            if (!intersection.isEmpty()) {
                linkStatus = 1;
                this.updateLinkData(info, intersection, linkStatus, mapKey, redisKey, redisValue, asset);
            }
            redisService.set(oldKey, redisValue);
        }
        return true;
    }

    private void updateLinkData(CollectCpuLoadBean info, Set<String> ipSet, int linkStatus, String serverPort, String redisKey, String redisValue, Asset asset) {

        for (String linkIp : ipSet) {
            AssetAppServer one = assetAppServerService.findOneLinkData(info.getAssetId(), Integer.parseInt(serverPort), linkIp);
            if (Objects.isNull(one)) {
                this.saveApp(info.getAssetId(), serverPort, linkIp, asset);
                continue;
            }
            if (one.getLinkStatus() == linkStatus) {
                continue;
            }
            one.setLinkStatus(linkStatus);
            assetAppServerService.updateById(one);
        }
        this.handleEvent(info, ipSet, redisKey, serverPort, linkStatus, redisValue);
    }

    private void saveLinkData(CollectCpuLoadBean info, Map.Entry<String, Set<String>> entry, Asset asset) {
        String assetId = info.getAssetId();
        List<AssetAppServer> list = assetAppServerService.findByAssetIdNullLink(assetId);
        if (list.isEmpty()) {
            return;
        }
        this.saveApp(info.getAssetId(), entry.getKey(), entry.getValue(), asset);
    }

    private void saveApp(String assetId, String serverPort, String linkIp, Asset asset) {
        Asset one = assetService.findOneByIp(linkIp);
        AssetAppServer assetAppServer = new AssetAppServer();
        assetAppServer.setId(MyIdUtil.getId());
        assetAppServer.setAssetId(assetId);
        assetAppServer.setAssetName(asset.getName());
        assetAppServer.setLinkAssetName(linkIp);
        assetAppServer.setLinkIp(linkIp);
        if (Objects.nonNull(one)) {
            assetAppServer.setLinkAssetName(one.getName());
        }
        assetAppServer.setServerPort(Integer.parseInt(serverPort));
        assetAppServer.setLinkStatus(1);
        assetAppServerService.save(assetAppServer);
    }

    private void saveApp(String assetId, String serverPort, Set<String> ipSet, Asset asset) {
        List<AssetAppServer> assetAppServers = new ArrayList<>();
        for (String linkIp : ipSet) {
            AssetAppServer server = assetAppServerService.findOneLinkData(assetId, Integer.parseInt(serverPort), linkIp);
            if (Objects.nonNull(server)) {
                continue;
            }

            Asset one = assetService.findOneByIp(linkIp);
            AssetAppServer assetAppServer = new AssetAppServer();
            assetAppServer.setId(MyIdUtil.getId());
            assetAppServer.setAssetId(assetId);
            assetAppServer.setAssetName(asset.getName());
            assetAppServer.setLinkAssetName(linkIp);
            assetAppServer.setLinkIp(linkIp);
            if (Objects.nonNull(one)) {
                assetAppServer.setLinkAssetName(one.getName());
            }
            assetAppServer.setServerPort(Integer.parseInt(serverPort));
            assetAppServer.setLinkStatus(1);
            assetAppServers.add(assetAppServer);
        }
        assetAppServerService.saveBatch(assetAppServers);
    }

    private void handleEvent(CollectCpuLoadBean info, Set<String> set, String redisKey, String mapKey, int linkStatus, String redisValue) {
        for (String oip : set) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(redisValue);
            changeInfo.setCollectTime(new Date());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            info.getMaps().put(mapKey, changeInfo);

            String eventRedisKey = StatusInfoChangeTypeEnum.event_appServer_link.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + mapKey + "_" + oip;

            Integer status = linkStatus == 0 ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();

            this.addEventStatus(StatusInfoChangeTypeEnum.event_appServer_link.getCode(), StatusInfoChangeTypeEnum.APP_LINK_STATUS.getCode(),
                    mapKey + "_" + oip, status, info, changeInfo);
            String str = status.equals(EventLevelEnum.ABNORMAL.getCode()) ? "丢失，" : "恢复，";

            String cpuload = "CPU负载信息：" + info.getCpuLoadOne() + "，" + info.getCpuLoadFive() + "，" + info.getCpuLoadFifteen();
            str += cpuload;

            Asset one = assetService.findOneByIp(oip);
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_appServer_link.getDescr(), mapKey, one == null ? oip : one.getName(), oip, str));
            alarmTempReq.setCollectValue(linkStatus + "");
            alarmTempReq.setFlag(mapKey);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq, info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_appServer_link.getDescr(), mapKey, one == null ? oip : one.getName(), oip, str));
                this.dispatureEvent(event);
            }
        }
    }

    private Set<String> getDifference(Set<String> set, Set<String> value) {
        Set<String> difference = new HashSet<>(set);
        difference.removeAll(value); // 保留 set 中存在但 value 中不存在的元素
        return difference;
    }

    /**
     * 直接控制下层处理
     *
     * @param flag
     * @return 返回true则需要下层处理，返回false不需要下层处理，并且不会保存缓存
     */
    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }
}
