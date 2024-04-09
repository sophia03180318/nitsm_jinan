package com.jcca.component.event.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.common.bean.constant.AlarmShowRecoverConst;
import com.jcca.common.bean.constant.EventGroupRecoverConst;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.event.EventAlarmGroupLogicService;
import com.jcca.component.event.bean.AddEventItem;
import com.jcca.component.event.bean.EventAlarmGroupExeQueueReq;
import com.jcca.component.event.bean.EventAlarmGroupQueueReq;
import com.jcca.component.event.bean.EventGetGroupResult;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.entity.AlarmEventGroup;
import com.jcca.web.event.entity.AlarmEventRel;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.enums.EventGroupLogicEnum;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.event.service.AlarmEventGroupService;
import com.jcca.web.event.service.AlarmEventRelService;
import com.jcca.web.event.service.AlarmEventService;
import com.jcca.web.event.service.AlarmEventTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 告警规则组业务 把告警细分到资产队列中
 *
 * @author lyp
 */
@Slf4j
@Service
public class EventAlarmGroupLogicServiceImpl implements EventAlarmGroupLogicService {

    public static final Integer ADD_FLG = 1;
    public static final Integer REMOVE_FLG = -1;

    private static final String SPLIT_FLAG = "-";

    @Resource
    private AlarmEventGroupService groupServ;
    @Resource
    private AlarmInfoService alarmServ;
    @Resource
    private AlarmEventRelService eventRelServ;
    @Resource
    private AlarmEventTypeService typeServ;
    @Resource
    private AlarmEventService eventInfoServ;
    @Resource
    private RedisService redisServ;
    @Resource
    private AssetService assetServ;
    @Resource
    private AlarmRepositoryService alarmRepoServ;

    @Override
    public void parseGroup(EventAlarmGroupQueueReq group) {
        List<AlarmEvent> newEventList = group.getNewEventList();

        for (AlarmEvent alarmEvent : newEventList) {
            // 循环触发的所有事件
            String eventTypeId = alarmEvent.getEventTypeId();
            List<AlarmEventGroup> groupList = groupServ.getAllByTypeId(eventTypeId);
            sendJob(alarmEvent, groupList);
        }

    }

    @Override
    public void exeAlarm(String queueStr) {

        EventAlarmGroupExeQueueReq queueBean = JSONUtil.toBean(JSONUtil.parseObj(queueStr),
                EventAlarmGroupExeQueueReq.class);

        String assetId = queueBean.getAlarmEvent().getAssetId();

        String lockKey = "ALARM_LOCK_KEY_" + assetId + "_EVENTTYPEID_" + queueBean.getAlarmEvent().getEventTypeId();
        synchronized (lockKey.intern()) {
            AlarmEvent alarmEvent = queueBean.getAlarmEvent();
            AlarmEventGroup group = queueBean.getGroup();

            Asset asset = assetServ.getById(alarmEvent.getAssetId());

            String eventTypeIds = group.getEventTypeIds();

            // 事件组内配置为可多条件触发的话需要在验证alarmCode
            Integer eventLevel = alarmEvent.getEventLevel();

            List<Integer> asList = Arrays.asList(EventLevelEnum.ABNORMAL.getCode(), EventLevelEnum.NOTIFY.getCode());

            boolean isAnd = EventGroupLogicEnum.AND.getCode().equals(group.getLogicalFlag());

            List<AlarmInfo> alarmList = new ArrayList<AlarmInfo>();

            EventGetGroupResult groupResult = null;

            if (asList.contains(eventLevel)) {
                // 查询是否可以上告警
                groupResult = getGroupResult(alarmEvent, eventTypeIds, isAnd, false);

                Boolean goAlarm = groupResult.getResult();
                // 查询数据库中该事件组存在的未恢复或未确认告警
                alarmList = findEventAlarmList(alarmEvent, group, groupResult);

                Boolean updateAlarm = false;
                // 通知、异常事件 满足与或关系直接上告警
                if (goAlarm) {
                    boolean isPingOtherStopEvent = EventUniqueCode.PING_OTHER_STOP.equals(alarmEvent.getUniqueCode());
                    boolean isProcessOtherStopEvent = EventUniqueCode.PROCESS_OTHER_STOP
                            .equals(alarmEvent.getUniqueCode());
                    boolean isInterfacesUpOrDownEvent = EventUniqueCode.INTERFACES_UP_DOWN_UNIQUE_CODE
                            .equals(alarmEvent.getUniqueCode());
                    boolean needUpdateAlarm = (alarmList.isEmpty() && isPingOtherStopEvent)
                            || (alarmList.isEmpty() && isProcessOtherStopEvent);

                    // 判定如果是串口虚拟口带:直接跳过
                    if (isInterfacesUpOrDownEvent) {
                        String flag = alarmEvent.getFlag();
                        if (StrUtil.isNotEmpty(flag)) {
                            String upperCase = flag.trim().toUpperCase();
                            if (upperCase.startsWith("S") && upperCase.contains(":") && upperCase.contains(".")) {
                                log.info("===跳过端口告警处理：{}==", flag);
                                return;
                            }
                        }

                    }

                    if (needUpdateAlarm) {
                        // 双机单端 ----更新设备信息
                        log.info("==双击单断触发==");
                        // 查询设备组中处于告警的另外一台设备ID
                        updateAlarm = updatePingAlarm(alarmEvent, group, asset, alarmList);
                    }

                    if (!updateAlarm) {
                        alarm(alarmEvent, group, alarmList);
                    }
                }
            } else if (EventLevelEnum.NORMAL.getCode().equals(eventLevel)) {
                // 查询是否可以上恢复
                groupResult = getGroupResult(alarmEvent, eventTypeIds, isAnd, true);

                log.info("资产{}【事件是否可上恢复请求】：事件信息：{},包含类型：{}，与或关系：{}", alarmEvent.getAssetId(), alarmEvent.getEventMsg(),
                        eventTypeIds, isAnd);
                log.info("资产{}【事件是否可上恢复运算结果】：是否可恢复：{}，是否需匹配标记：{}", alarmEvent.getAssetId(), groupResult.getResult(),
                        groupResult.getNeedMatchAlarm());

                // 查询数据库中该事件组存在的未恢复或未确认告警
                alarmList = findEventAlarmList(alarmEvent, group, groupResult);

                // 恢复事件 满足与或关系直接上恢复
                Boolean goNormal = groupResult.getResult();
                if (goNormal) {
                    recovery(alarmList, alarmEvent, group, asset);
                }
            }

            if (Objects.nonNull(groupResult)) {
                List<AlarmEvent> correlationEvent = groupResult.getCorrelationEvent();
                for (AlarmInfo alarm : alarmList) {
                    List<String> eventIds = eventRelServ.getAllEventIdByAlarmId(alarm.getId());
                    for (AlarmEvent item : correlationEvent) {
                        if (eventIds.contains(item.getId())) {
                            continue;
                        }
                        eventIds.add(item.getId());

                        AlarmEventRel rel = new AlarmEventRel();
                        rel.setId(MyIdUtil.getId());
                        rel.setAlarmId(alarm.getId());
                        rel.setEventId(item.getId());
                        rel.setCreateTime(item.getCreateTime());

                        eventRelServ.save(rel);
                    }
                }
            }

        }
    }

    private List<AlarmInfo> findEventAlarmList(AlarmEvent alarmEvent, AlarmEventGroup group,
                                               EventGetGroupResult groupResult) {
        List<AlarmInfo> alarmList = new ArrayList<AlarmInfo>();
        if (groupResult.getNeedMatchAlarm()) {
            String eventAlarmCode = alarmServ.getEventAlarmCode(group.getId(), alarmEvent.getFlag());
            alarmList = alarmServ.findValidAlarmByCorrElationId(group.getId(), alarmEvent.getAssetId(), eventAlarmCode);
        } else {
            alarmList = alarmServ.findValidAlarmByCorrElationId(group.getId(), alarmEvent.getAssetId());
        }
        return alarmList;
    }

    /**
     * 更新告警信息
     * <p>
     * 返回结果：如果同组存在其他告警返回true，不存在返回false
     * <p>
     * 返回false 需要新建告警
     *
     * @param alarmEvent
     * @param group
     * @param asset
     */
    private Boolean updatePingAlarm(AlarmEvent alarmEvent, AlarmEventGroup group, Asset asset,
                                    List<AlarmInfo> alarmList) {
        QueryWrapper<Asset> queryWrapper = new QueryWrapper<Asset>();
        queryWrapper.eq("ASSET_CODE", asset.getAssetCode());
        queryWrapper.eq("IS_DEL", 1);
        queryWrapper.ne("ID", asset.getId());
        List<Asset> list = assetServ.list(queryWrapper);
        if (list.isEmpty()) {
            return false;
        }
        Asset normalAsset = list.get(0);
        List<AlarmInfo> alarms = alarmServ.findValidAlarmByCorrElationId(group.getId(), normalAsset.getId());

        return updateAlarmMsg(alarmEvent, group, asset, alarmList, alarms);
    }

    /**
     * 更新告警所在资产
     *
     * @param alarmEvent
     * @param group
     * @param asset
     * @param alarmList
     * @param alarms
     * @return
     */
    private Boolean updateAlarmMsg(AlarmEvent alarmEvent, AlarmEventGroup group, Asset asset, List<AlarmInfo> alarmList,
                                   List<AlarmInfo> alarms) {
        if (!alarms.isEmpty()) {
            AlarmInfo alarminfo = alarms.get(0);
            String alarmMsg = alarmServ.formatAlarmMsg(group, alarmEvent, asset);
            alarminfo.setAssetId(asset.getId());
            alarminfo.setAssetIp(asset.getIp());
            alarminfo.setAssetName(asset.getName());
            alarminfo.setOrgId(asset.getOrgId());
            alarminfo.setDescription(alarmMsg);
            alarminfo.setContent(alarmMsg);
            alarminfo.setAlarmState(AlarmStateEnum.ALARM.getCode());
            alarminfo.setIsShowRecover(AlarmShowRecoverConst.NO_SHOW);
            alarminfo.setOccurTime(alarmEvent.getCreateTime());

            alarmServ.updateById(alarminfo);

            alarmList.add(alarminfo);

            return true;
        }

        return false;
    }

    /**
     * 恢复
     *
     * @param alarmList
     */
    private void recovery(List<AlarmInfo> alarmList, AlarmEvent alarmEvent, AlarmEventGroup group, Asset asset) {
        if (EventGroupRecoverConst.CANNOT.equals(group.getRecoverFlag())) {
            log.error("【事件处理】：告警不可恢复，忽略此正常事件");
            return;
        }
        List<String> asList = Arrays.asList(EventUniqueCode.PING_OTHER_STOP, EventUniqueCode.PROCESS_OTHER_STOP);

        List<AlarmInfo> list = new ArrayList<AlarmInfo>();
        Boolean needUpdateOrRecover = true;
        // 双击单断告警判断另外一个设备是否处于告警 是的话更新
        if (EventUniqueCode.PING_OTHER_STOP.equals(alarmEvent.getUniqueCode())) {
            list = alarmServ.findValidAlarmByCorrElationId(group.getId(), asset.getId());
            needUpdateOrRecover = needUpdateOrRecover(list, alarmEvent, group, asset, asList);
        } else {
            needUpdateOrRecover = needUpdateOrRecover(alarmList, alarmEvent, group, asset, asList);
        }

        if (!needUpdateOrRecover) {
            return;
        }

        // ping的单独处理
        for (AlarmInfo alarmInfo : list) {
            alarmServ.exeAgainEventAlarm(group, alarmInfo, 1, alarmEvent);
            // 关联一下事件
            AlarmEventRel rel = new AlarmEventRel();
            rel.setId(MyIdUtil.getId());
            rel.setAlarmId(alarmInfo.getId());
            rel.setEventId(alarmEvent.getId());
            rel.setCreateTime(alarmEvent.getCreateTime());

            eventRelServ.save(rel);
        }

        for (AlarmInfo item : alarmList) {
            alarmServ.exeAgainEventAlarm(group, item, 1, alarmEvent);
        }
    }

    /**
     * 双击单断告警判断另外一个设备是否处于告警 是的话更新 不是的话返回true继续执行
     *
     * @param alarmList
     * @param alarmEvent
     * @param group
     * @param asset
     * @param asList
     * @return 是否需要继续执行恢复
     */
    private Boolean needUpdateOrRecover(List<AlarmInfo> alarmList, AlarmEvent alarmEvent, AlarmEventGroup group,
                                        Asset asset, List<String> asList) {
        String uniqueCode = alarmEvent.getUniqueCode();
        if (asList.contains(uniqueCode)) {
            QueryWrapper<Asset> queryWrapper = new QueryWrapper<Asset>();
            queryWrapper.eq("ASSET_CODE", asset.getAssetCode());
            queryWrapper.eq("IS_DEL", 1);
            queryWrapper.ne("ID", asset.getId());
            List<Asset> list = assetServ.list(queryWrapper);

            if (list.isEmpty()) {
                return true;
            }
            Asset errorAsset = list.get(0);

            AddEventItem req = new AddEventItem();
            req.setRepo(alarmRepoServ.getById(alarmEvent.getRepositoryId()));
            req.setType(typeServ.getById(alarmEvent.getEventTypeId()));
            AlarmEvent lastLog = eventInfoServ.getTypeLastLog(req, errorAsset.getId(), null);

            if (Objects.isNull(lastLog)) {
                return true;
            }

            if (EventLevelEnum.ABNORMAL.getCode().equals(lastLog.getEventLevel())) {
                // update
                List<AlarmInfo> needUpdate = new ArrayList<AlarmInfo>();
                updateAlarmMsg(lastLog, group, errorAsset, needUpdate, alarmList);
                if (!needUpdate.isEmpty()) {
                    alarmServ.updateBatchById(needUpdate);
                }
                return false;
            }

        }
        return true;
    }

    /**
     * 组设备告警逻辑
     */
    private void alarm(AlarmEvent alarmEvent, AlarmEventGroup group, List<AlarmInfo> alarmList) {
        if (!alarmList.isEmpty()) {
            // 更新告警状态
            for (AlarmInfo alarmInfo : alarmList) {
                alarmInfo.setLastTime(alarmEvent.getCreateTime());
                alarmInfo.setOccurTime(alarmEvent.getCreateTime());
                alarmServ.exeAgainEventAlarm(group, alarmInfo, -1, alarmEvent);
            }

            alarmServ.updateBatchById(alarmList);
            return;
        }

        String alarmInfoId = alarmServ.exeEventAlarm(group, alarmEvent);

        AlarmInfo alarm = alarmServ.getById(alarmInfoId);
        alarmList.add(alarm);

    }

    /**
     * 与或逻辑核心
     * <p>
     * 分析一组内与、或结果 正常或不正常
     *
     * @param alarmEvent
     * @param eventTypeIds
     * @param isAnd
     * @param normal
     */
    private EventGetGroupResult getGroupResult(AlarmEvent alarmEvent, String eventTypeIds, Boolean isAnd,
                                               Boolean normal) {
        EventGetGroupResult result = new EventGetGroupResult();
        result.setResult(true);
        result.setNeedMatchAlarm(false);

        /** 需要关联的事件id列表 **/
        List<AlarmEvent> needLinkAlarmList = new ArrayList<AlarmEvent>();
        needLinkAlarmList.add(alarmEvent);

        List<String> typeArrayList = new ArrayList<String>();
        List<String> normalTypeList = new ArrayList<String>();
        List<String> errorTypeList = new ArrayList<String>();
        // 分析是否需要匹配告警码
        Boolean needMatchAlarmCode = true;
        String groupFlag = alarmEvent.getMatchFlag();

        String[] typeList = eventTypeIds.split(SPLIT_FLAG);
        if (typeList.length == 0) {
            return result;
        }

        for (String typeId : typeList) {
            typeArrayList.add(typeId);

            AlarmEventType type = typeServ.getById(typeId);

            Boolean eventStatus = true;
            if (Objects.isNull(type)) {
                eventStatus = true;
                needMatchAlarmCode = false;
            } else {
                eventStatus = eventInfoServ.getEventStatus(alarmEvent.getAssetId(), typeId, alarmEvent.getFlag(), alarmEvent);

                AddEventItem req = new AddEventItem();
                req.setRepo(alarmRepoServ.getById(alarmEvent.getRepositoryId()));
                req.setType(typeServ.getById(alarmEvent.getEventTypeId()));
                AlarmEvent event = eventInfoServ.getTypeLastLog(req, alarmEvent.getAssetId(), alarmEvent.getFlag());

                needLinkAlarmList.add(event);
                if (Objects.nonNull(event) && needMatchAlarmCode) {
                    needMatchAlarmCode = groupFlag.equals(event.getMatchFlag());
                }
            }

            if (eventStatus && !normalTypeList.contains(typeId)) {
                normalTypeList.add(typeId);
            } else if (!eventStatus && !errorTypeList.contains(typeId)) {
                errorTypeList.add(typeId);
            }
        }

        if (isAnd) {
            if (normal) {
                // 与逻辑获取是否可恢复--有一个正常结果则可以恢复
                result.setResult(!normalTypeList.isEmpty());
            } else {
                // 与逻辑获取是否告警--所有配置项都异常才告警
                result.setResult(errorTypeList.size() == typeArrayList.size());
            }
        } else {
            if (normal) {
                // 或逻辑获取是否可恢复--结果都正常则可以恢复
                result.setResult(normalTypeList.size() == typeArrayList.size());
            } else {
                // 或逻辑获取是否告警--有一个告警则告警
                result.setResult(!errorTypeList.isEmpty());
            }
        }

        // 是否需要匹配
        result.setNeedMatchAlarm(needMatchAlarmCode);
        result.setCorrelationEvent(needLinkAlarmList);

        return result;
    }

    /**
     * 下发告警任务
     *
     * @param alarmEvent
     * @param groupList
     */
    private void sendJob(AlarmEvent alarmEvent, List<AlarmEventGroup> groupList) {
        // 循环单个事件触发的告警规则组
        for (AlarmEventGroup roleItem : groupList) {
            // 分发到资产事件告警处理队列
            EventAlarmGroupExeQueueReq req = new EventAlarmGroupExeQueueReq();
            req.setAlarmEvent(alarmEvent);
            req.setGroup(roleItem);

            String assetId = alarmEvent.getAssetId();

            Asset asset = assetServ.getById(assetId);
            if (StrUtil.isNotEmpty(asset.getAssetCode())) {
                // 检查是否有同组设备
                List<Asset> assetList = assetServ.listByAssetCode(asset.getAssetCode());
                if (!assetList.isEmpty()) {
                    assetId = asset.getAssetCode();
                }
            }

            String queueCode = RedisQueueConst.EVENT_GROUP_ALARM_EXE;

            redisServ.convertAndSend(queueCode, JSONUtil.toJsonStr(req));

        }
    }

}
