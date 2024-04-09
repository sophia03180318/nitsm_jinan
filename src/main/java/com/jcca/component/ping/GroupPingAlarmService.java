package com.jcca.component.ping;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.enums.AssetModeEnum;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.other.bean.PingAssetStatus;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.enums.CollectNetCardStatus;
import com.jcca.web.collect.service.CollectNetworkCardService;
import com.jcca.web.db.service.ManageDbService;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.ip.service.IpInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName PingAlarmService
 * @Description PING告警处理
 * @Date 2020/6/22 14:32
 * @Author hanwone
 */
//@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class GroupPingAlarmService {

    @Resource
    private AssetService assetService;
    @Resource
    private EventLogicService eventLogicServ;
    @Resource
    private IpInfoService ipInfoServ;
    @Resource
    private CollectNetworkCardService netCardServ;
    @Resource
    private ManageDbService managerDbServ;

    // ping 告警处理锁池
    private static Map<String, Object> pingLockPool = new ConcurrentHashMap<>();

    public void handle(ReceiveAlarmDto alarmData) {
        log.info("【分组ping处理参数】：{}", JSONUtil.toJsonStr(alarmData));

        if (StrUtil.isEmpty(alarmData.getContent())) {
            log.error("ping 告警组信息不能为空");
            return;
        }

        List<PingAssetStatus> pingAssetStatusList = JSONUtil.toList(JSONUtil.parseArray(alarmData.getContent()),
                PingAssetStatus.class);

        String lockKey = ReceiveAlarmTypeEnum.PING.getName() + "_"
                + pingAssetStatusList.get(0).getAsset().getAssetCode();
        pingLockPool.putIfAbsent(lockKey, new Object());
        Object lock = pingLockPool.get(lockKey);

        synchronized (lock) {
            // 更新设备ping状态,更新Asset
            for (int i = 0; i < pingAssetStatusList.size(); i++) {
                String assetIp = pingAssetStatusList.get(i).getAsset().getIp();
                Asset dbAsset = assetService.findOneByIp(assetIp);

                if (Objects.isNull(dbAsset)) {
                    log.error("IP为[{}]的资产不存在", assetIp);
                    return;
                }
                if (dbAsset.getWatch() == 0) {
                    log.info("【ping告警非监控资产，不推送】：{}", dbAsset.getIp());
                    return;
                }

                Boolean currStatus = pingAssetStatusList.get(i).getCurrStatus();
                if (currStatus) {
                    dbAsset.setStatus(StatusConst.OK);

                    if (AssetModeEnum.SERVER.getCode() == dbAsset.getAssetMode()) {
                        //更新网卡状态
                        netCardServ.updateNetCardStatus(dbAsset.getId(), dbAsset.getIp(), CollectNetCardStatus.UP);
                    }
                } else {
                    dbAsset.setStatus(StatusConst.NO);

                    if (AssetModeEnum.SERVER.getCode() == dbAsset.getAssetMode()) {
                        //更新网卡状态
                        netCardServ.updateNetCardStatus(dbAsset.getId(), dbAsset.getIp(), CollectNetCardStatus.UP);
                    }

                }
                assetService.updateById(dbAsset);
                pingAssetStatusList.get(i).setAsset(dbAsset);
            }

            List<Asset> groupAssetList = new ArrayList<Asset>();
            List<Asset> downAssetList = new ArrayList<Asset>();
            List<Asset> recoverAssetList = new ArrayList<Asset>();

            for (PingAssetStatus pingAssetStatus : pingAssetStatusList) {
                groupAssetList.add(pingAssetStatus.getAsset());

                if (false == pingAssetStatus.getCurrStatus()) {
                    downAssetList.add(pingAssetStatus.getAsset());
                    continue;
                }

                recoverAssetList.add(pingAssetStatus.getAsset());
            }

            int pingDownCount = downAssetList.size();

            // 全部掉线
            if (pingDownCount >= pingAssetStatusList.size()) {
                // 所有设备上告警
                addAllDownEvent(alarmData, downAssetList);
                return;

            } else if (pingDownCount > 0) {
                // 部分掉线、推送双击单断异常事件
                addOtherDownEvent(alarmData, groupAssetList, downAssetList, recoverAssetList);
                return;

            }

            // 没有设备掉线
            for (Asset normalAsset : groupAssetList) {
                String msg = String.format("设备组内设备ip:%s状态正常", normalAsset.getIp());
                CreateEventReq eventReq = new CreateEventReq();
                eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                eventReq.setAssetId(normalAsset.getId());
                eventReq.setUniqueCode(EventUniqueCode.PING_OTHER_STOP);
                //eventReq.setFlag(normalAsset.getIp());
                eventReq.setFlag(normalAsset.getId());
                eventReq.setCreateTime(DateUtil.parse(alarmData.getOccurTime(), "yyyy-MM-dd HH:mm:ss"));
                eventReq.setOriginalMsg(msg);
                eventReq.setGroupFlag(EventGroupConstant.PING);

                CreateEventReq copy = EntityBeanUtil.copy(eventReq, CreateEventReq.class);
                copy.setUniqueCode(EventUniqueCode.PING_ALL_STOP);

                try {

                    eventLogicServ.addEvent(eventReq);
                    eventLogicServ.addEvent(copy);
                } catch (Exception e) {
                    log.error("处理PING事件失败", e);
                }
            }

        }
    }

    /**
     * 处理组内部分断事件
     *
     * @param alarmData
     * @param groupAssetList
     * @param downAssetList
     */
    private void addOtherDownEvent(ReceiveAlarmDto alarmData, List<Asset> groupAssetList, List<Asset> downAssetList, List<Asset> recoverAssetList) {
        for (Asset asset : downAssetList) {
            String msg = String.format("设备组内设备:%s,IP:%s掉线", asset.getName(), asset.getIp());
            CreateEventReq eventReq = new CreateEventReq();
            eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
            eventReq.setAssetId(asset.getId());
            eventReq.setUniqueCode(EventUniqueCode.PING_OTHER_STOP);
            eventReq.setGroupFlag(EventGroupConstant.PING);
            //eventReq.setFlag(asset.getIp());
            eventReq.setFlag(asset.getId());
            eventReq.setCreateTime(DateUtil.parse(alarmData.getOccurTime(), "yyyy-MM-dd HH:mm:ss"));

            eventReq.setOriginalMsg(msg);

            try {
                eventLogicServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("处理组PING事件失败", e);
            }
        }

        //未断设备的恢复
        for (Asset asset : recoverAssetList) {
            String msg = String.format("设备组内设备:%s,IP:%s 状态恢复！网络状态正常！", asset.getName(), asset.getIp());
            CreateEventReq eventReq = new CreateEventReq();
            eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
            eventReq.setAssetId(asset.getId());
            eventReq.setUniqueCode(EventUniqueCode.PING_OTHER_STOP);
            eventReq.setGroupFlag(EventGroupConstant.PING);
            //eventReq.setFlag(asset.getIp());
            eventReq.setFlag(asset.getId());
            eventReq.setCreateTime(DateUtil.parse(alarmData.getOccurTime(), "yyyy-MM-dd HH:mm:ss"));

            eventReq.setOriginalMsg(msg);

            try {
                eventLogicServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("处理组PING事件失败", e);
            }
        }

        // 和双击双断恢复事件
        for (Asset asset : groupAssetList) {
            String msg = String.format("设备%s所在组-网络正常", asset.getName(), asset.getIp());
            CreateEventReq eventReq = new CreateEventReq();
            eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
            eventReq.setAssetId(asset.getId());
            eventReq.setUniqueCode(EventUniqueCode.PING_ALL_STOP);
            eventReq.setGroupFlag(EventGroupConstant.PING);
            eventReq.setFlag(asset.getId());
            eventReq.setCreateTime(DateUtil.parse(alarmData.getOccurTime(), "yyyy-MM-dd HH:mm:ss"));

            eventReq.setOriginalMsg(msg);

            try {
                eventLogicServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("处理组PING事件失败", e);
            }
        }
    }

    /**
     * 处理组内全断事件
     *
     * @param alarmData
     * @param downAssetList
     */
    private void addAllDownEvent(ReceiveAlarmDto alarmData, List<Asset> downAssetList) {
        for (Asset asset : downAssetList) {
            String msg = "设备组内设备全部掉线";
            CreateEventReq eventReq = new CreateEventReq();
            eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
            eventReq.setAssetId(asset.getId());
            eventReq.setUniqueCode(EventUniqueCode.PING_ALL_STOP);
            eventReq.setGroupFlag(EventGroupConstant.PING);
            eventReq.setFlag(asset.getId());
            eventReq.setCreateTime(DateUtil.parse(alarmData.getOccurTime(), "yyyy-MM-dd HH:mm:ss"));

            eventReq.setOriginalMsg(msg);

            // 全部设备都发一遍单机单断时间，防止少写入异常事件
            String tmpMsg = String.format("设备组内设备:%s,IP:%s掉线", asset.getName(), asset.getIp());
            CreateEventReq copy = EntityBeanUtil.copy(eventReq, CreateEventReq.class);
            copy.setUniqueCode(EventUniqueCode.PING_OTHER_STOP);
            copy.setOriginalMsg(tmpMsg);

            try {
                eventLogicServ.addEvent(eventReq);
                eventLogicServ.addEvent(copy);
            } catch (Exception e) {
                log.error("处理PING事件失败", e);
            }

        }
    }

}
