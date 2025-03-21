package com.jcca.dataProcessing.DataFilter.ping;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CommonEntity;
import com.jcca.dataProcessing.Entity.ReceiveAlarmEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Zhaozheng
 * @description TODO 组ping过滤处理类
 * @className pingGroupFilterHandler
 * @date 2023/10/27 9:51
 * @since 2.1.0.0
 */
@Component("pingGroupAllDownFilterHandler")
public class PingGroupAllDownFilterHandler extends IFilterHandler<ReceiveAlarmEntity> {

    private static final Integer MIN_GROUP_SIZE = 2;

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;


    @Override
    public boolean handler(ReceiveAlarmEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "组ping全断", info.getAssetIp());
        //全断才是异常
        boolean status = false;
        boolean isGroup = false;
        for (Map.Entry<String, ChangeInfo> entry : info.getMaps().entrySet()) {
            String key = entry.getKey();
            ChangeInfo changeInfo = entry.getValue();
            if (key.contains(StatusInfoChangeTypeEnum.group_single_status_ping.getCode()) && !key.contains(StatusInfoChangeTypeEnum.event_ping.getCode())) {
                isGroup = true;
                if ((boolean) changeInfo.getValue() == true) {
                    status = true;
                }
            }
        }
        //无组ping信息
        if (isGroup == false) {
            return true;
        }


        Integer eventStatus = status ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
        List<ChangeInfo> list = new ArrayList<>();
            for (Map.Entry<String, ChangeInfo> entry : info.getMaps().entrySet()) {
                ChangeInfo changeInfo = entry.getValue();
                if (entry.getKey().contains(StatusInfoChangeTypeEnum.group_single_status_ping.getCode())) {


                    list.add(changeInfo);

                }
            }
        if (list.size() > 0) {
            for (ChangeInfo change : list) {
                ChangeInfo changeInfoAll = new ChangeInfo();
                changeInfoAll.setValue(eventStatus);
                changeInfoAll.setRedisKey(change.getRedisKey());
                changeInfoAll.setMapKey(StatusInfoChangeTypeEnum.group_all_status_ping.getCode());
                changeInfoAll.setCollectTime(new Date());
                info.getMaps().put(changeInfoAll.getMapKey() + "_" + change.getRedisKey().split(":")[0], changeInfoAll);

                String eventRedisKey = StatusInfoChangeTypeEnum.event_ping_group_all.getCode();
                String eventMapKey = change.getMapKey() + "_" + change.getRedisKey().split(":")[0];
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_ping_group_all.getDescr()));
                alarmTempReq.setCollectValue(changeInfoAll.getValue().toString());
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(change.getRedisKey().split(":")[1], changeInfoAll, eventRedisKey, eventMapKey, eventStatus,alarmTempReq);
                if (event != null) {
                    //被事件信息截取
                    changeInfoAll.setIsEvent(true);
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_ping_group_all.getDescr()));
                    this.dispatureEvent(event);
                }


                info.setAssetIp(change.getRedisKey().split(":")[0]);
                info.setAssetId(change.getRedisKey().split(":")[1]);
                this.addEventStatus(eventRedisKey, eventStatus, info, changeInfoAll);
            }
        }


        return status;//如果全是异常的不需要，判断部分异常直接返回false，不需要pingGroupOtherDownFilterHandler处理
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }
    
    public void addEventStatus(String mapKey, Object value, CommonEntity info, ChangeInfo change) {
        ChangeInfo changeEventStatus = new ChangeInfo();
        changeEventStatus.setValue(value);
        changeEventStatus.setRedisKey(change.getRedisKey().split(":")[0] + ":" + change.getRedisKey().split(":")[1] + ":" + StatusInfoChangeTypeEnum.statusEvent.getCode());
        changeEventStatus.setMapKey(mapKey);
        if (info.getCollectTime() != null) {
            changeEventStatus.setCollectTime(new Date(info.getCollectTime()));
        } else {
            changeEventStatus.setCollectTime(new Date());
        }
        info.getMaps().put(mapKey + "_" + change.getRedisKey().split(":")[0], changeEventStatus);

        ChangeInfo changeEventStatusValue = new ChangeInfo();
        changeEventStatusValue.setValue(change.getValue());
        changeEventStatusValue.setRedisKey(change.getRedisKey().split(":")[0] + ":" + change.getRedisKey().split(":")[1] + ":" + StatusInfoChangeTypeEnum.statusEventValue.getCode());
        changeEventStatusValue.setMapKey(mapKey + "." + change.getMapKey());
        if (info.getCollectTime() != null) {
            changeEventStatus.setCollectTime(new Date(info.getCollectTime()));
        } else {
            changeEventStatus.setCollectTime(new Date());
        }
        info.getMaps().put(changeEventStatusValue.getMapKey() + "_" + change.getRedisKey().split(":")[0], changeEventStatusValue);
    }

    ;


}
