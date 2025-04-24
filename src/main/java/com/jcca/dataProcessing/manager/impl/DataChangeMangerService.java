package com.jcca.dataProcessing.manager.impl;

import com.jcca.common.bean.constant.AlarmBlankConst;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.enums.AlarmStatusEnum;
import com.jcca.common.enums.AlarmTypeEnum;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.EventAlarmLevelBaseEntity;
import com.jcca.dataProcessing.Entity.EventInfo;
import com.jcca.dataProcessing.manager.IDataChangeManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.construction.service.ConstructionRecordService;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.entity.AlarmEventRel;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.event.service.AlarmEventRelService;
import com.jcca.web.event.service.AlarmEventService;
import com.jcca.web.event.service.AlarmEventTypeService;
import com.jcca.web2.service.IndexPageService;
import com.jcca.web2.vo.AlarmSocketVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 数据变动管理
 * @className AlarmMangerService
 * @date 2023/10/26 19:05
 * @since 2.1.0.0
 */
@Service
public class DataChangeMangerService implements IDataChangeManagerService {

    @Resource
    private AlarmInfoService alarmInfoService;
    @Resource
    private AssetService assetServ;
    @Resource
    private AlarmEventTypeService alarmEventTypeService;
    @Resource
    private EventInfoManagerService eventInfoManagerService;
    @Resource
    private AlarmEventService eventServ;
    @Resource
    private AlarmEventRelService relServ;
    @Resource
    private IndexPageService indexPageServ;
    @Resource
    private ConstructionRecordService constructionRecordServ;


    @Override
    public AlarmEvent saveEvent(IEvent info) {
        AlarmEvent alarmEvent = createEvent(info);
        eventServ.save(alarmEvent);
        return alarmEvent;
    }


    /**
     * 关联事件
     *
     * @param alarmInfoId
     * @param alarmEvent
     */
    @Override
    public void linkEvent(String alarmInfoId, AlarmEvent alarmEvent) {
        AlarmEventRel rel = new AlarmEventRel();
        rel.setAlarmId(alarmInfoId);
        rel.setEventId(alarmEvent.getId());
        rel.setCreateTime(new Date());
        rel.setId(MyIdUtil.getId());
        relServ.save(rel);
    }

    /**
     * 向前端推送告警
     *
     * @param asset
     * @param newAlarm
     */
    @Override
    public void popup(Asset asset, AlarmInfo newAlarm) {
        Boolean blank = constructionRecordServ.isBlank(asset.getId(), newAlarm.getOccurTime());
        if (blank) {
            //天窗告警，不推送
            return;
        }

        AlarmSocketVo vo = new AlarmSocketVo();
        try {
            //v2推送
            vo.setAssetName(newAlarm.getAssetName());
            vo.setAssetId(newAlarm.getAssetId());
            vo.setIsJcca(asset.isJccaAsset());
            vo.setAlarmLevel(newAlarm.getAlarmLevel().intValue());
            vo.setAlarmTitle(newAlarm.getTitle());
            vo.setStatus(AlarmStatusEnum.UNCONFIRM.getCode());
            vo.setAlarmState(AlarmStateEnum.ALARM.getCode());
            vo.setNewAlarm(AlarmSocketVo.YES);
            indexPageServ.sendAlarmMsg(vo);
            //v1
            indexPageServ.sendAlarmMsgV1(vo);
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.ALARM_POP, vo, e);
        }
    }

    @Override
    public void saveInfo(Map<String, ChangeInfo> maps) {
        for (Map.Entry<String, ChangeInfo> entry : maps.entrySet()) {
            ChangeInfo changeInfo = entry.getValue();
            //如果事件信息未截取，则保存信息
            if (changeInfo.getIsEvent() == null || changeInfo.getIsEvent() == false) {
                eventInfoManagerService.setStateValue(changeInfo.getRedisKey(), changeInfo.getMapKey(), changeInfo.getValue());
            }
        }

    }

    @Override
    public AlarmInfo saveAlarm(IEvent event, Asset asset) {

        EventAlarmLevelBaseEntity eventAlarmLevelBaseEntity = event.getEventAlarmLevelBaseEntity();
        AlarmTempReq alarmTempReq = event.getAlarmTempReq();
        alarmTempReq.setAssetName(asset.getName());
        alarmTempReq.setAssetIp(asset.getIp());
        alarmTempReq.setAssetGroupCode(asset.getAssetCode());
        String content = alarmTempReq.formatting(eventAlarmLevelBaseEntity.getTemplateStr());

        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setId(MyIdUtil.getId());
        //取事件类型名称
        alarmInfo.setTitle(eventAlarmLevelBaseEntity.getEventTypeName());
        alarmInfo.setAlarmLevel(eventAlarmLevelBaseEntity.getAlarmLevel().byteValue());
        alarmInfo.setStatus(AlarmStatusEnum.UNCONFIRM.getCode());
        alarmInfo.setAlarmState(AlarmStateEnum.ALARM.getCode());
        alarmInfo.setAssetId(event.getAssetId());
        alarmInfo.setAlarmCode(event.getRedisKey());
        alarmInfo.setType(AlarmTypeEnum.HARDWARE.getCode());

        alarmInfo.setContent(content);
        alarmInfo.setDescription(event.getDescStr());
        alarmInfo.setAlarmFlag(event.getMapKey());
        alarmInfo.setOpinion(eventAlarmLevelBaseEntity.getOpinion());

        String[] split = event.getRedisKey().split(":");
        if (split.length > 1) {
            alarmInfo.setEventCategory(split[0] + ":" + split[1]);
        } else {
            alarmInfo.setEventCategory(event.getRedisKey());
        }
        alarmInfo.setAssetIp(asset.getIp());
        alarmInfo.setAssetName(asset.getName());
        alarmInfo.setOrgId(asset.getOrgId());

        Boolean blank = constructionRecordServ.isBlank(asset.getId(), event.getCollectTime());
        alarmInfo.setOccurTime(event.getCollectTime());
        alarmInfo.setLastTime(event.getCollectTime());
        alarmInfo.setBlank(blank ? AlarmBlankConst.BLANK : AlarmBlankConst.NORMARL);

        alarmInfo.setCreateTime(new Date());
        alarmInfo.setCreator("root");
        alarmInfo.setModifyTime(new Date());
        alarmInfo.setModifier("root");

        alarmInfoService.save(alarmInfo);

        return alarmInfo;
    }

    private AlarmEvent createEvent(IEvent event) {
        ChangeInfo changeInfo = (ChangeInfo) event.getInfo();
        EventAlarmLevelBaseEntity eventAlarmLevelBaseEntity = event.getEventAlarmLevelBaseEntity();
        AlarmEvent alarmEvent = new AlarmEvent();
        alarmEvent.setId(MyIdUtil.getId());
        alarmEvent.setAssetId(event.getAssetId());
        alarmEvent.setEventLevel(event.getStatus());

        if (Objects.nonNull(eventAlarmLevelBaseEntity)) {
            //未知事件typeId和repoId都是空
            if (Objects.isNull(event.getEventAlarmLevelBaseEntity().getAlarmLevel())) {
                //没有级别  本应该置未空，但是为了满足现场条件，改为 9
                alarmEvent.setEventLevel(EventLevelEnum.UNKNOW.getCode());
            }
            alarmEvent.setEventTypeId(eventAlarmLevelBaseEntity.getEventTypeId());
            alarmEvent.setRepositoryId(eventAlarmLevelBaseEntity.getRepoId());
        } else {
            AlarmEventType type = alarmEventTypeService.queryUnkonwEvent();
            alarmEvent.setEventTypeId(type.getId());
        }
        if (event.getInfo() != null && event.getInfo().getEventInfo() != null) {//告警源是从管理口推送过来的
            EventInfo eventInfo = event.getInfo().getEventInfo();
            alarmEvent.setEventMsg(eventInfo.getMessage());
            alarmEvent.setRepoMsg(eventInfo.getMessage());
        } else {
            alarmEvent.setEventMsg(event.getDescStr());
            alarmEvent.setRepoMsg(event.getDescStr());
        }

        alarmEvent.setUniqueCode(event.getRedisKey());
        alarmEvent.setFlag(event.getMapKey());
        alarmEvent.setCreateTime(event.getCollectTime());
        alarmEvent.setUpdateTime(new Date());
        alarmEvent.setCreateTime(new Date());

        alarmEvent.setBaseValue("");
        if (Objects.nonNull(changeInfo.getValue())) {
            alarmEvent.setCollectValue(changeInfo.getValue().toString());
        }

        return alarmEvent;


    }
}
