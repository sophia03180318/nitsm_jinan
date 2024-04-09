package com.jcca.component.event.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.redis.queue.RedisQueueTemplate;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.component.client.StationCollectClient;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.event.CodeService;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.*;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.event.service.AlarmEventService;
import com.jcca.web.event.service.AlarmEventTypeService;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.cluster.NoopCacheCluster;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 事件逻辑实现
 *
 * @author lyp
 */
@Slf4j
@Service
public class EventLogicServiceImpl implements EventLogicService {

    public static final Integer ADD_FLG = 1;
    public static final Integer REMOVE_FLG = -1;

    // 通知时间的时间间隔-分钟
    private static final Integer NOTIFY_INTERVAL = 5;
    private static final List<String> EXCEPTION_CODE = Arrays.asList("DS_ERROR", "RAID_ERROR");

    @Resource
    private AlarmEventService eventInfoServ;
    @Resource
    private AlarmEventTypeService eventTypeServ;
    @Resource
    private RedisService redisService;
    @Resource
    private AssetService assetServ;
    @Resource
    private StationCollectClient stationCollectClient;
    @Resource
    private CodeService codeServ;


    @Override
    public boolean sendAlarmOrRecoverEventV2(CreateEventReq req) {
        try {
            //先查找这个事件命中哪些知识库
            List<AddEventItem> eventTypeList = eventTypeServ.getAllEventTypeByMsg(req.getUniqueCode(), req.getOriginalMsg());
            if (eventTypeList.isEmpty()) {
                //没有此事件
                AlarmEventType unKnowEvent = eventTypeServ.queryUnkonwEvent();
                AddEventItem item = new AddEventItem();
                item.setType(unKnowEvent);
                item.setRepo(null);
                eventTypeList.add(item);
            }
            List<AlarmEvent> newEventList = new ArrayList<AlarmEvent>();
            for (AddEventItem addEventItem : eventTypeList) {
                AlarmRepository repo = addEventItem.getRepo();
                AlarmEventType type = addEventItem.getType();

                AlarmEvent alarmEvent = this.creatEvent(req, type, repo.getId());
                newEventList.add(alarmEvent);
            }
            if (!newEventList.isEmpty()) {
                eventInfoServ.saveBatch(newEventList);

                EventAlarmGroupQueueReq queueReq = new EventAlarmGroupQueueReq();
                queueReq.setNewEventList(newEventList);
                redisService.convertAndSend(RedisQueueConst.EVENT_GROUP_ALARM, JSONUtil.toJsonStr(queueReq));
            }
            return true;
        } catch (Exception e) {
            if (LogInputUtils.inputError(ServerTypeEnum.ALARM_INFO)) {
                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.ALARM_INFO, ErrorCodeEnum.JOB_UNHEALTHY_ASSET_02, req.getAsset().getIp(), e.getMessage()), e);
            }
        }
        return false;
    }


    @Override
    public void addEvent(CreateEventReq req) throws Exception {
        // 分发到资产队列
        String assetId = req.getAssetId();
        Asset asset = assetServ.getById(assetId);

        if (Objects.isNull(asset) || !StatusEnum.OK.getCode().equals(asset.getIsDel())) {
            log.error("【处理ADD事件失败】：-----》资产不存在:[" + assetId + "]");
            return;
        }
        if (Objects.isNull(req.getCreateTime())) {
            log.error("【处理ADD事件失败】：-----》发生时间空");
            return;
        }
        if (StrUtil.isEmpty(req.getFlag())) {
            req.setFlag("--");
        }

        AddEventQueueBean copy = EntityBeanUtil.copy(req, AddEventQueueBean.class);

        List<AddEventItem> eventTypeList = eventTypeServ.getAllEventTypeByMsg(req.getUniqueCode(), req.getOriginalMsg());

        if (eventTypeList.isEmpty()) {
            AlarmEventType unkonwEvent = eventTypeServ.queryUnkonwEvent();

            AddEventItem item = new AddEventItem();
            item.setType(unkonwEvent);
            item.setRepo(null);
            eventTypeList.add(item);

        }
        copy.setTypeList(eventTypeList);

        String queueName = RedisQueueConst.EVENT_GROUP_ALARM_ADD;
        redisService.convertAndSend(queueName, JSONUtil.toJsonStr(copy));

    }

    /**
     * 处理队列中的数据
     * <p>
     * 保存事件
     */
    @Override
    public void addEventQueue(AddEventQueueBean req) {

        List<AddEventItem> eventTypeList = req.getTypeList();
        CreateEventResult createEventResult = createEventList(req, eventTypeList);

        List<AlarmEvent> alarmQueueList = createEventResult.getAlarmQueueList();
        List<AlarmEvent> newEventList = createEventResult.getSaveList();
        List<AlarmEvent> needNotifyList = createEventResult.getNeedNotifyList();

        // save
        if (!newEventList.isEmpty()) {
            try {
                eventInfoServ.saveBatch(newEventList);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return;
            }

        }
        if (!alarmQueueList.isEmpty()) {
            // 事件放到队列等待事件告警处理器处理
            EventAlarmGroupQueueReq queueReq = new EventAlarmGroupQueueReq();
            queueReq.setNewEventList(alarmQueueList);

            redisService.convertAndSend(RedisQueueConst.EVENT_GROUP_ALARM, JSONUtil.toJsonStr(queueReq));
        }

        //不在告警队列中的
        for (AlarmEvent alarmEvent : needNotifyList) {
            List<Integer> codeList = Arrays.asList(EventLevelEnum.ABNORMAL.getCode(), EventLevelEnum.WARNING.getCode());
            stationCollectClient.notifyStationPingStatus(alarmEvent.getAssetId(),!codeList.contains(alarmEvent.getEventLevel()),alarmEvent.getUniqueCode());
            stationCollectClient.notifyStationAlarmStatus(alarmEvent.getAssetId(),!codeList.contains(alarmEvent.getEventLevel()),alarmEvent.getUniqueCode());
        }


    }


    /**
     * 分析数据，新建事件，过滤不需要新建的事件
     *
     * @param req
     * @param eventTypeList
     * @return
     */
    private CreateEventResult createEventList(AddEventQueueBean req, List<AddEventItem> eventTypeList) {
        List<AlarmEvent> saveList = new ArrayList<AlarmEvent>();
        List<AlarmEvent> notifyList = new ArrayList<AlarmEvent>();
        List<AlarmEvent> alarmList = new ArrayList<AlarmEvent>();
        for (AddEventItem item : eventTypeList) {
            AlarmEventType type = item.getType();
            AlarmRepository repo = item.getRepo();

            // 未知事件直接保存
            if (Objects.isNull(repo)) {
                if (EventLevelEnum.NOTIFY.getCode().equals(req.getEventLevel())) {
                    String cacheKey = eventInfoServ.getCacheKey(req.getAssetId(), type.getId(), req.getFlag(),
                            req.getUniqueCode(), null);

                    if (redisService.exists(cacheKey) && !EXCEPTION_CODE.contains(req.getUniqueCode())) {
                        continue;
                    }
                    redisService.set(cacheKey, cacheKey, NOTIFY_INTERVAL * 60L);
                } else if (!EventLevelEnum.WARNING.getCode().equals(req.getEventLevel())) {
                    // 防止事件重复添加-通知事件除外
                    AlarmEvent event = eventInfoServ.getLastLog(item, req.getAssetId(), req.getFlag(), req.getUniqueCode());
                    if (Objects.nonNull(event)) {
                        if (event.getEventLevel().equals(req.getEventLevel())&& event.getUniqueCode().equals(req.getUniqueCode())) {
                            AlarmEvent newEvent = creatEvent(req, type, null);
                            notifyList.add(newEvent);
                            continue;
                        }
                    } else {
                        // 过滤首次正常的
                        if (EventLevelEnum.NORMAL.getCode().equals(req.getEventLevel())) {
                            AlarmEvent newEvent = creatEvent(req, type, null);
                            notifyList.add(newEvent);
                            continue;
                        }
                    }
                }

                AlarmEvent newEvent = creatEvent(req, type, null);
                saveList.add(newEvent);
                notifyList.add(newEvent);

                continue;
            }

            if (!req.getEventLevel().equals(EventLevelEnum.WARNING.getCode())) {
                req.setEventLevel(repo.getFlagType());
            }

            AlarmEvent event = eventInfoServ.getLastLog(item, req.getAssetId(), req.getFlag(), req.getUniqueCode());

            // 首次触发
            if (Objects.isNull(event)) {
                // 过滤首次正常的
                if (EventLevelEnum.NORMAL.getCode().equals(req.getEventLevel())) {
                    // 首次正常事件--加入缓存 不保存
                    List<String> oncesNoAlarmCodeList = Arrays.asList(EventUniqueCode.NETWORK_UP_DOWN_UNIQUE_CODE,
                            EventUniqueCode.INTERFACES_UP_DOWN_UNIQUE_CODE);
                    AlarmEvent creatEvent = creatEvent(req, type, repo.getId());
                    if (oncesNoAlarmCodeList.contains(req.getUniqueCode())) {
                        saveList.add(creatEvent);
                    }
                    notifyList.add(creatEvent);
                    continue;
                }

                AlarmEvent creatEvent = creatEvent(req, type, repo.getId());
                saveList.add(creatEvent);
                alarmList.add(creatEvent);

                continue;
            }

            if (EventLevelEnum.WARNING.getCode().equals(req.getEventLevel())) {
                AlarmEvent creatEvent = creatEvent(req, type, repo.getId());
                saveList.add(creatEvent);
                notifyList.add(creatEvent);
                continue;
            }


            // 重复触发-检验变动
            boolean levelVerify = event.getEventLevel().equals(req.getEventLevel());

            if (!(levelVerify && !EventLevelEnum.NOTIFY.getCode().equals(req.getEventLevel()))) {
                AlarmEvent creatEvent = creatEvent(req, type, repo.getId());
                saveList.add(creatEvent);
                alarmList.add(creatEvent);
            }


            if(levelVerify){
                //上送了ping并且状态没有任何变动 可能发送通知失败了重新发送
                AlarmEvent creatEvent = creatEvent(req, type, repo.getId());
                notifyList.add(creatEvent);
            }
        }

        CreateEventResult result = new CreateEventResult();
        result.setAlarmQueueList(alarmList);
        result.setSaveList(saveList);
        result.setNeedNotifyList(notifyList);

        return result;
    }

    /**
     * 对象数据初始化
     *
     * @param req
     * @param alarmEventType
     * @param repositoryId
     * @return
     */
    private AlarmEvent creatEvent(CreateEventReq req, AlarmEventType alarmEventType, String repositoryId) {
        AlarmEvent newEvent = new AlarmEvent();
        newEvent.setId(codeServ.getCode("ALARM_EVENT", 9999L));
        newEvent.setUpdateTime(new Date());
        newEvent.setAssetId(req.getAssetId());
        newEvent.setCreateTime(req.getCreateTime());
        newEvent.setEventLevel(req.getEventLevel());
        newEvent.setEventMsg(req.getOriginalMsg());
        newEvent.setEventTypeId(alarmEventType.getId());
        newEvent.setRepositoryId(repositoryId);
        newEvent.setRemark("");
        newEvent.setFlag(req.getFlag());
        newEvent.setRepoMsg(req.getRepoMsg());
        newEvent.setUniqueCode(req.getUniqueCode());
        newEvent.setCollectValue(req.getCollectValue());
        newEvent.setBaseValue(req.getBaseValue());
        newEvent.setRemark(req.getRemark());
        newEvent.setMatchFlag(req.getGroupFlag());

        // 在此维护一个缓存事件表，缓存入库最新的事件
        String cacheKey = eventInfoServ.getCacheKey(req.getAssetId(), alarmEventType.getId(), req.getFlag(),
                req.getUniqueCode(), repositoryId);
        redisService.set(cacheKey, newEvent, 86400L);

        return newEvent;
    }

}
