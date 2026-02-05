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
import java.util.stream.Collectors;

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
            int port = Integer.parseInt(entry.getKey());
            Set<String> newIpSet = entry.getValue();
            List<AssetAppServer> oldValue = assetAppServerService.findByAssetIdAndServerPort2(assetId, port);
            HashSet<String> oldIpSet = oldValue.stream().filter(a -> a.getLinkStatus() == 1).map(AssetAppServer::getLinkIp).collect(Collectors.toCollection(HashSet::new));

            Set<String> alarmList = this.getDifference(oldIpSet, newIpSet);
            if (!alarmList.isEmpty()) {
                String redisKey = info.getAssetIp() + ":" + assetId + ":" + StatusInfoChangeTypeEnum.event_appServer_link.getCode() + ":" + port;
                for (String ip : alarmList) {
                    AssetAppServer one = assetAppServerService.findOneLinkData(info.getAssetId(), port, ip);
                    one.setLinkStatus(0);
                    assetAppServerService.updateById(one);
                    //推告警
                    handleEvent(info, ip, redisKey, entry.getKey(), 0, "");
                }

            }
            Set<String> recoverList = this.getDifference(newIpSet, oldIpSet);
            if (!recoverList.isEmpty()) {
                for (String ip : recoverList) {
                    AssetAppServer one = assetAppServerService.findOneLinkData(info.getAssetId(), port, ip);
                    if (Objects.isNull(one)) {
                        this.saveApp(info.getAssetId(), port, ip, asset);
                        continue;
                    }
                    one.setLinkStatus(1);
                    assetAppServerService.updateById(one);
                }
            }

        }
        return true;
    }

    private void handleEvent(CollectCpuLoadBean info, String oip, String redisKey, String mapKey, int linkStatus, String redisValue) {
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
        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq, info.getInspectRecordId(), info.getVersion());
        if (event != null) {
            //被事件信息截取
            changeInfo.setIsEvent(true);
            event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_appServer_link.getDescr(), mapKey, one == null ? oip : one.getName(), oip, str));
            this.dispatureEvent(event);
        }
    }

    private void saveApp(String assetId, int serverPort, String linkIp, Asset asset) {
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
        assetAppServer.setServerPort(serverPort);
        assetAppServerService.save(assetAppServer);
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
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
