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
@Component("pingGroupOtherDownFilterHandler")
public class PingGroupOtherDownFilterHandler extends IFilterHandler<ReceiveAlarmEntity> {

    private static final Integer MIN_GROUP_SIZE = 2;

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;


    @Override
    public boolean handler(ReceiveAlarmEntity info) {
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "组ping模式单断", info.getAssetIp());
        //部分ping不通才是异常
        boolean status = true;
        boolean isGroup = false;
        for (Map.Entry<String, ChangeInfo> entry : info.getMaps().entrySet()) {
            String key = entry.getKey();
            ChangeInfo changeInfo = entry.getValue();
            if (key.contains(StatusInfoChangeTypeEnum.group_single_status_ping.getCode()) && !key.contains(StatusInfoChangeTypeEnum.event_ping.getCode())) {
                isGroup = true;
                if ((boolean) changeInfo.getValue() == false) {
                    status = false;
                }
            }
        }
        //无组ping信息
        if (isGroup == false) {
            return true;
        }

        Integer eventStatus = status ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
        List<ChangeInfo> list = new ArrayList<>();
        if(info.getMaps()!=null&&info.getMaps().size()>0) {
            for (Map.Entry<String, ChangeInfo> entry : info.getMaps().entrySet()) {
                ChangeInfo changeInfo = entry.getValue();
                if (entry.getKey().contains(StatusInfoChangeTypeEnum.group_single_status_ping.getCode()) && !entry.getKey().contains(StatusInfoChangeTypeEnum.event_ping.getCode())) {//过滤掉组ping正常信息

                    list.add(changeInfo);

                }
            }
        }

        if (list.size() > 0) {
            for (ChangeInfo change : list) {
                ChangeInfo changeInfoOther = new ChangeInfo();
                changeInfoOther.setValue(eventStatus);
                changeInfoOther.setRedisKey(change.getRedisKey());
                changeInfoOther.setMapKey(StatusInfoChangeTypeEnum.group_other_status_ping.getCode());
                changeInfoOther.setCollectTime(new Date());
                info.getMaps().put(changeInfoOther.getMapKey() + "_" + change.getRedisKey().split(":")[0], changeInfoOther);

                String eventRedisKey = StatusInfoChangeTypeEnum.event_ping_group_other.getCode();
                String eventMapKey = changeInfoOther.getMapKey() + "_" + change.getRedisKey().split(":")[0];
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_ping_group_other.getDescr()));
                alarmTempReq.setCollectValue(changeInfoOther.getValue().toString());
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(change.getRedisKey().split(":")[1], changeInfoOther, eventRedisKey, eventMapKey, eventStatus,alarmTempReq);
                if (event != null) {
                    //被事件信息截取
                    changeInfoOther.setIsEvent(true);
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_ping_group_other.getDescr()));
                    this.dispatureEvent(event);
                }

                info.setAssetIp(change.getRedisKey().split(":")[0]);
                info.setAssetId(change.getRedisKey().split(":")[1]);
                this.addEventStatus(eventRedisKey, eventStatus, info, changeInfoOther);
            }
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return true;
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
