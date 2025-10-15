package com.jcca.web.common.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.jcca.admin.system.service.TopoAssetPortService;
import com.jcca.common.bean.constant.AlarmBlankConst;
import com.jcca.common.bean.constant.StatusConst;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.common.enums.AlarmTypeEnum;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.client.StationCollectClient;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.alarm.service.AlarmInfoService;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.AssetHidConf;
import com.jcca.web.asset.service.AssetHidConfService;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.collect.enums.InterfaceStatus;
import com.jcca.web.common.constants.StationAlarmUniqueCodeEnum;
import com.jcca.web.common.controller.req.StationAlarmReqV1;
import com.jcca.web.common.controller.req.StationAlarmReqV2;
import com.jcca.web.common.controller.req.StationAlarmResp;
import com.jcca.web.common.service.StationAlarmService;
import com.jcca.web.construction.service.ConstructionRecordService;
import com.jcca.web.cycles.service.CyclesInfoService;
import com.jcca.web.event.entity.AlarmEvent;
import com.jcca.web.event.entity.AlarmEventGroup;
import com.jcca.web.event.entity.AlarmEventRel;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.enums.EventGroupLogicEnum;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.event.enums.EventTypeStatusEnum;
import com.jcca.web.event.service.AlarmEventGroupService;
import com.jcca.web.event.service.AlarmEventRelService;
import com.jcca.web.event.service.AlarmEventService;
import com.jcca.web.event.service.AlarmEventTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * @description: 处理车站告警
 * @author: Lvyp
 * @create: 2024/04/29 16:29
 */
@Service
public class StationAlarmServiceImpl implements StationAlarmService {

    private static final String STATION_ALARM_UNIQUE = "STATION_PUSH_ALARM";

    @Resource
    private AssetService assetService;
    @Resource
    private AssetHidConfService hidConfServ;

    @Resource
    private AlarmInfoService alarmInfoServ;
    @Resource
    private AlarmEventService alarmEventServ;
    @Resource
    private AlarmEventRelService relServ;
    @Resource
    private AlarmEventGroupService eventGroupServ;
    @Resource
    private AlarmRepositoryService alarmRepoServ;
    @Resource
    private AlarmEventTypeService eventTypeServ;
    @Resource
    private ConstructionRecordService constructionRecordService;
    @Resource
    private TopoAssetPortService topoAssetPortServ;
    @Resource
    private StationCollectClient stationCollectClient;
    @Resource
    private CyclesInfoService cyclesInfoServ;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void disposePingAlarm(StationAlarmReqV1 req) {

        if(StrUtil.isEmpty(req.getAssetId()) || StrUtil.isEmpty(req.getCreateTime())){
            return ;
        }
        //校验资产是否存在
        Asset asset = assetService.getById(req.getAssetId());
        if(Objects.isNull(asset)){
            return ;
        }
        Integer eventLevel = req.getEventLevel();
        Date occurTime =new Date(Long.valueOf(req.getCreateTime()));
        AlarmRepository repository = initAlarmRepo();

        String alarmCode = req.getFlag()+":"+asset.getId()+":"+asset.getIp();
        StationAlarmReqV2 eventReq = new StationAlarmReqV2();
        eventReq.setAlarmDescription(req.getOriginalMsg());
        eventReq.setAlarmRecoverStatus(eventLevel);
        eventReq.setFlag(req.getFlag());
        eventReq.setAlarmTitle("车站告警");
        eventReq.setAlarmLevel(2);
        eventReq.setAlarmStatus(1);
        eventReq.setAssetId(req.getAssetId());
        eventReq.setAlarmType(AlarmTypeEnum.HARDWARE.getCode());
        eventReq.setAlarmCode(alarmCode);


        //创建事件
        AlarmEvent event = createEvent(asset, repository, eventReq, occurTime);
        //处理告警

        AlarmInfo alarmInfo = alarmInfoServ.selectUnOverAlarm(alarmCode);

        if(eventLevel<0){
            if(Objects.nonNull(alarmInfo)){
                //历史存在已经恢复情况的告警  新建一个事件，关联此告警，并更新告警状态
                if(AlarmStateEnum.RECOVER.getCode().intValue() == alarmInfo.getAlarmState()){
                    alarmInfo.setAlarmState(AlarmStateEnum.ALARM.getCode());
                    updateAlarmInfo(alarmInfo,event);
                }
            }else{
                //新的告警
                AlarmInfo newAlarmInfo = createAlarmInfo(eventReq, asset, event.getEventTypeId(), occurTime);
                saveAlarmInfo(newAlarmInfo,event);
            }

            if(asset.getStatus() == StatusConst.OK){
                asset.setStatus(StatusConst.NO);
                assetService.updateById(asset);
            }
        }else if(Objects.nonNull(alarmInfo) && AlarmStateEnum.ALARM.getCode().intValue() == alarmInfo.getAlarmState()){
            //新上恢复
            alarmInfo.setAlarmState(AlarmStateEnum.RECOVER.getCode());
            updateAlarmInfo(alarmInfo,event);

            if(asset.getStatus() == StatusConst.NO){
                asset.setStatus(StatusConst.OK);
                assetService.updateById(asset);
            }
        }

        //通知车站
        List<Integer> codeList = Arrays.asList(EventLevelEnum.ABNORMAL.getCode(), EventLevelEnum.WARNING.getCode());
        stationCollectClient.notifyStationPingStatus(req.getAssetId(),!codeList.contains(req.getEventLevel()),req.getUniqueCode());
        stationCollectClient.notifyStationAlarmStatus(req.getAssetId(),!codeList.contains(req.getEventLevel()),req.getUniqueCode());
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public StationAlarmResp disposeStationAlarm(StationAlarmReqV2 req) {
        StationAlarmResp resp = new StationAlarmResp();
        resp.setId(req.getId());
        resp.setStatus(StationAlarmResp.PushAlarmStatusEnum.SUCCESS.name());

        String assetId = req.getAssetId();
        //校验资产是否存在
        Asset asset = assetService.getById(assetId);
        if(Objects.isNull(asset)){
            resp.setItsmId("-1");
            return resp;
        }
        Integer stationAlarmRecoverStatus = req.getAlarmRecoverStatus();

        //校验是否是需要过滤的告警  端口屏蔽、网卡屏蔽
        List<AssetHidConf> hidConfigList = hidConfServ.getHidConfigByAssetAndFlag(req.getAssetId(), req.getFlag());
        if(!hidConfigList.isEmpty()){
            resp.setStatus(StationAlarmResp.PushAlarmStatusEnum.REFUSE.name());
            return resp;
        }
        DateTime occurTime = DateUtil.parse(req.getOccurTimeStr(), "yyyyMMddHHmmss");
        AlarmRepository repository = initAlarmRepo();
        //创建事件
        AlarmEvent event = createEvent(asset, repository, req, occurTime);
        //处理告警
        AlarmInfo alarmInfo = alarmInfoServ.selectUnOverAlarm(req.getAlarmCode());
        if(Objects.nonNull(alarmInfo)){
            resp.setItsmId(alarmInfo.getId());
        }


        if(AlarmStateEnum.ALARM.getCode().intValue() == stationAlarmRecoverStatus){
            if(Objects.nonNull(alarmInfo)){
                //历史存在已经恢复情况的告警  新建一个事件，关联此告警，并更新告警状态
                if(AlarmStateEnum.RECOVER.getCode().intValue() == alarmInfo.getAlarmState()){
                    alarmInfo.setAlarmState(AlarmStateEnum.ALARM.getCode());
                    updateAlarmInfo(alarmInfo,event);
                }
            }else{
                //新的告警
                AlarmInfo newAlarmInfo = createAlarmInfo(req, asset, event.getEventTypeId(), occurTime);
                saveAlarmInfo(newAlarmInfo,event);

                resp.setItsmId(newAlarmInfo.getId());
            }
        }else if(Objects.nonNull(alarmInfo) && AlarmStateEnum.ALARM.getCode().intValue() == alarmInfo.getAlarmState()){
            //新上恢复
            alarmInfo.setAlarmState(AlarmStateEnum.RECOVER.getCode());
            updateAlarmInfo(alarmInfo,event);
        }


        //更新资产的监控状态
        if(StationAlarmUniqueCodeEnum.PING_STOP.name().equals(req.getUniqueCode())){
            if(AlarmStateEnum.ALARM.getCode().intValue() == stationAlarmRecoverStatus){
                if(asset.getStatus() == StatusConst.OK){
                    asset.setStatus(StatusConst.NO);
                    assetService.updateById(asset);
                }
            }else{
                if(asset.getStatus() == StatusConst.NO){
                    asset.setStatus(StatusConst.OK);
                    assetService.updateById(asset);
                }
            }
        }else if(StationAlarmUniqueCodeEnum.INTERFACES_STATUS.name().equals(req.getUniqueCode())){
            //TOPO更新端口状态
            if(AlarmStateEnum.ALARM.getCode().intValue() != stationAlarmRecoverStatus){
                topoAssetPortServ.updatePortStatus(assetId, req.getFlag(),
                        InterfaceStatus.OK.getCode().intValue());
            } else {
                topoAssetPortServ.updatePortStatus(assetId, req.getFlag(),
                        InterfaceStatus.NO.getCode().intValue());
            }
        }

        return resp;
    }



    /**
     * 保存告警信息
     * @param alarmInfo
     * @param event
     */
    private void saveAlarmInfo(AlarmInfo alarmInfo,AlarmEvent event){
        AlarmEventRel rel = new AlarmEventRel();
        rel.setAlarmId(alarmInfo.getId());
        rel.setEventId(event.getId());
        rel.setId(MyIdUtil.getId());
        rel.setCreateTime(new Date());

        alarmInfoServ.save(alarmInfo);
        alarmEventServ.save(event);
        relServ.save(rel);
    }

    /**
     * 更新告警信息
     * @param alarmInfo
     * @param event
     */
    private void updateAlarmInfo(AlarmInfo alarmInfo,AlarmEvent event){
        AlarmEventRel rel = new AlarmEventRel();
        rel.setAlarmId(alarmInfo.getId());
        rel.setEventId(event.getId());
        rel.setId(MyIdUtil.getId());
        rel.setCreateTime(new Date());

        alarmInfoServ.updateById(alarmInfo);
        alarmEventServ.save(event);
        relServ.save(rel);
    }

    /**
     * 初始化告警知识库
     * @return
     */
    private AlarmRepository initAlarmRepo(){
        AlarmEventType type = eventTypeServ.getById("2");
        if(Objects.isNull(type)){
            type = new AlarmEventType();
            type.setId("2");
            type.setName("车站上送告警");
            type.setDescStr("车站上报的告警信息，请勿修改。");
            type.setStatus(EventTypeStatusEnum.USED.getCode());
            type.setCreateTime(new Date());
            eventTypeServ.save(type);
        }

        List<AlarmRepository> allByAlarmCode = alarmRepoServ.getAllByAlarmCode(STATION_ALARM_UNIQUE);
        if(allByAlarmCode.isEmpty()){
            AlarmRepository repository = new AlarmRepository();
            repository.setId(MyIdUtil.getId());
            repository.setName("车站上报告警规则，请勿修改");
            repository.setAlarmLevel(2);
            repository.setAlarmCode(STATION_ALARM_UNIQUE);
            repository.setStatusFlag("*");
            repository.setFlagType(EventLevelEnum.WARNING.getCode());
            repository.setEventTypeId("2");
            repository.setDescStr("车站告警");
            repository.setPlanStr("根据告警描述信息进行问题排查");
            repository.setCreateTime(new Date());
            repository.setCreator("system");
            alarmRepoServ.save(repository);

            return repository;
        }

        return allByAlarmCode.get(0);
    }


    /**
     * 创建事件
     * @param asset
     * @param repository
     * @param req
     * @param occurTime
     */
    private AlarmEvent createEvent(Asset asset, AlarmRepository repository, StationAlarmReqV2 req, Date occurTime){
        AlarmEvent event = new AlarmEvent();
        event.setId(MyIdUtil.getId());
        event.setAssetId(asset.getId());
        event.setEventTypeId(repository.getEventTypeId());
        event.setRepositoryId(repository.getId());
        event.setEventMsg(req.getAlarmDescription());
        event.setRepoMsg(req.getAlarmDescription());
        event.setUniqueCode(STATION_ALARM_UNIQUE);
        if(StrUtil.isEmpty(req.getFlag())){
            event.setFlag(asset.getIp()+"_"+asset.getId());
        }else{
            event.setFlag(asset.getIp()+"_"+asset.getId()+"_"+req.getFlag());
        }
        event.setFlag(req.getFlag());
        if(req.getAlarmRecoverStatus()==1){
            event.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
        }else{
            event.setEventLevel(EventLevelEnum.NORMAL.getCode());
        }
        event.setRemark("车站上送告警信息");
        event.setCreateTime(occurTime);
        event.setUpdateTime(occurTime);

        return event;
    }

    /**
     * 创建告警信息
     * @param req
     * @return
     */
    private AlarmInfo createAlarmInfo(StationAlarmReqV2 req, Asset asset, String typeId, Date occurTime){
        byte blank = AlarmBlankConst.NORMARL;
        boolean haveBlank = constructionRecordService.isBlank(asset.getId(), occurTime);
        if (haveBlank) {
            blank = AlarmBlankConst.BLANK;
        }

        List<AlarmEventGroup> group = eventGroupServ.getAllByTypeId(typeId);
        AlarmEventGroup eventGroup = null;
        if(group.isEmpty()){
            eventGroup = new AlarmEventGroup();
            eventGroup.setId(MyIdUtil.getId());
            eventGroup.setName("车站告警");
            eventGroup.setAlarmType(AlarmTypeEnum.HARDWARE.getCode());
            eventGroup.setEventTypeIds(typeId);
            eventGroup.setLevle(2);
            eventGroup.setMsgTemp("上送车站原始告警信息，不支持编辑");
            eventGroup.setRecoverFlag(1);
            eventGroup.setLogicalFlag(EventGroupLogicEnum.AND.getCode());
            eventGroup.setUseStage(-1);
            eventGroup.setCreateDate(new Date());
            eventGroupServ.save(eventGroup);
        }else{
            eventGroup = group.get(0);
        }

        String alarmId = MyIdUtil.getId();
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setId(alarmId);
        alarmInfo.setTitle(req.getAlarmTitle());
        alarmInfo.setAlarmLevel(req.getAlarmLevel().byteValue());
        alarmInfo.setStatus(req.getAlarmStatus().byteValue());
        alarmInfo.setAlarmState(req.getAlarmStatus().byteValue());
        alarmInfo.setAssetId(req.getAssetId());
        alarmInfo.setAssetIp(asset.getIp());
        alarmInfo.setAssetName(asset.getName());
        alarmInfo.setOrgId(asset.getOrgId());
        alarmInfo.setType(req.getAlarmType());
        alarmInfo.setOccurTime(occurTime);
        alarmInfo.setLastTime(occurTime);
        alarmInfo.setAlarmCode(req.getAlarmCode());
        alarmInfo.setDescription(req.getAlarmDescription());
        alarmInfo.setContent(req.getAlarmDescription());
        alarmInfo.setBlank(blank);
        alarmInfo.setCorrelationId(eventGroup.getId());
        alarmInfo.setCreateTime(new Date());
        alarmInfo.setCreator("openApi");
        return alarmInfo;
    }


}
