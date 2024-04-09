package com.jcca.component.ping;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.enums.AssetModeEnum;
import com.jcca.common.enums.OrgTypeEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.component.enums.ReceiveAlarmTypeEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.alarm.dao.AlarmInfoMapper;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.asset.controller.bean.AlarmVerifyBean;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.enums.CollectNetCardStatus;
import com.jcca.web.collect.service.CollectNetworkCardService;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.ip.enums.IpPingStatusEnum;
import com.jcca.web.ip.service.IpInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName PingAlarmService
 * @Description PING告警处理 采集器控制PING状态变化，当PING状态有变化时才向ITSM推送告警。 *
 * 例：当前状态为通，过会断了，会推送一条；当前状态为断，过会儿通了，才会推送一条。的
 * @Date 2020/6/22 14:32
 * @Author hanwone
 */
//@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class NoGroupPingAlarmService {

    @Resource
    private AssetService assetService;
    @Resource
    private AlarmInfoMapper alarmInfoMapper;
    @Resource
    private IpInfoService ipInfoServ;
    @Resource
    private EventLogicService eventLogicServ;
    @Resource
    private SysOrgService orgService;

    @Resource
    private CollectNetworkCardService netCardServ;
    @Resource
    private RedisService redisService;

    @Resource
    private SysModuleConfigService sysModuleServ;
    private static Map<String, Integer> alarmCounterMap = new HashMap<String, Integer>();


    // ping 告警处理锁池
    private static Map<String, Object> pingLockPool = new ConcurrentHashMap<>();

    public void handle(ReceiveAlarmDto alarmData) {
        log.info("无分组ping告警处理参数{}", JSONUtil.toJsonStr(alarmData));
        String reqAssetIp = alarmData.getAssetIp();

        Asset asset = assetService.findOneByIp(reqAssetIp);
        if (Objects.isNull(asset)) {
            log.error("IP为[{}]的资产不存在", reqAssetIp);
            return;
        }
        if (asset.getWatch() == 0) {
            log.info("【ping告警非监控资产，不推送】：{}", asset.getIp());
            return;
        }
        String alarmCode = ReceiveAlarmTypeEnum.PING.getName() + "_" + asset.getId();
        pingLockPool.putIfAbsent(alarmCode, new Object());
        Object lock = pingLockPool.get(alarmCode);
        synchronized (lock) {
            // 验证此ping告警是否被同步 redis ping告警线程处理过
            Map<String, Object> map = new HashMap<>();
            map.put("assetId", asset.getId());
            AlarmInfo alarmInfo = alarmInfoMapper.getLastPingAlarmInfo(map);
            if (alarmInfo != null && alarmInfo.getOccurTime().getTime() >= DateUtil
                    .parseDateTime(alarmData.getOccurTime()).getTime()) {
                log.info("此ping告警已不是最新告警,拒绝入库");
                return;
            }
        }
        if (alarmData.getFlag()) {
            //移除计数器
            alarmCounterMap.remove(reqAssetIp);

            // 更新设备状态
            asset.setStatus(StatusConst.OK);

//            ipInfoServ.updatePingStatus(asset.getIp(), IpPingStatusEnum.USED);
            assetService.updateById(asset);

            if (AssetModeEnum.SERVER.getCode() == asset.getAssetMode()) {
                //更新网卡状态
                netCardServ.updateNetCardStatus(asset.getId(), asset.getIp(), CollectNetCardStatus.UP);
            }

            // 在线
            String msg = "设备PING结果：通";

            CreateEventReq eventReq = new CreateEventReq();
            eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
            eventReq.setAssetId(asset.getId());
            eventReq.setUniqueCode(EventUniqueCode.PING_STOP);
            eventReq.setGroupFlag(EventGroupConstant.PING);
            //eventReq.setFlag(asset.getIp());
            eventReq.setFlag(asset.getId());
            eventReq.setCreateTime(DateUtil.parse(alarmData.getOccurTime(), "yyyy-MM-dd HH:mm:ss"));

            eventReq.setOriginalMsg(msg);

            try {
                eventLogicServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("处理PING事件失败", e);
            }

            return;
        } else {
            // 更新设备状态
            asset.setStatus(StatusConst.NO);
//            ipInfoServ.updatePingStatus(asset.getIp(), IpPingStatusEnum.UNUSED);
            assetService.updateById(asset);
            //校验计数器
            AlarmVerifyBean alarmVerifyConf = sysModuleServ.getAlarmVerifyValue();
            SysOrg org = orgService.getById(asset.getOrgId());
            if (OrgTypeEnum.STATION.getCode() == org.getType()) {
                alarmVerifyConf.setPingSize(0);
            }

            Integer alarmCount = alarmCounterMap.get(reqAssetIp);
            if (Objects.isNull(alarmCount)) {
                alarmCount = 0;
            }

            CreateEventReq eventReq = new CreateEventReq();

            String msg = "设备PING结果：断";
            if (alarmCount == alarmVerifyConf.getPingSize() || alarmCount > alarmVerifyConf.getPingSize()) {
                //告警
                asset.setStatus(StatusConst.NO);
                ipInfoServ.updatePingStatus(asset.getIp(), IpPingStatusEnum.UNUSED);
                assetService.updateById(asset);
                if (AssetModeEnum.SERVER.getCode() == asset.getAssetMode()) {
                    //更新网卡状态
                    netCardServ.updateNetCardStatus(asset.getId(), asset.getIp(), CollectNetCardStatus.DOWN);
                }
                // 中断
                eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
            } else {
                //删除redis 让他重新推一次
                redisService.delPingStatus(asset.getId());

                eventReq.setEventLevel(EventLevelEnum.WARNING.getCode());
                msg = String.format("设备PING结果：断，检查到设备PING第%s次中断，在设定检查次数范围内，不告警！", alarmCount + 1);
                alarmCounterMap.put(reqAssetIp, alarmCount + 1);
            }

            eventReq.setAssetId(asset.getId());
            eventReq.setUniqueCode(EventUniqueCode.PING_STOP);
            eventReq.setFlag(asset.getId());
            eventReq.setGroupFlag(EventGroupConstant.PING);
            eventReq.setCreateTime(DateUtil.parse(alarmData.getOccurTime(), "yyyy-MM-dd HH:mm:ss"));

            eventReq.setOriginalMsg(msg);

            try {
                eventReq.setGroupFlag(EventGroupConstant.PING);

                eventLogicServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("处理PING事件失败", e);
            }
        }

    }
}
