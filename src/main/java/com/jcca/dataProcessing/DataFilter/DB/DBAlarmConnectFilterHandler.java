package com.jcca.dataProcessing.DataFilter.DB;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectDBEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 数据库连接状态过滤处理类
 * @className DBConnectFilterHandler
 * @date 2023/10/27 9:30
 * @since 2.1.0.0
 */
@Component("dBAlarmConnectFilterHandler")
public class DBAlarmConnectFilterHandler extends IFilterHandler<CollectDBEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectDBEntity info) {
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_db.getCode();
        String mapKey = StatusInfoChangeTypeEnum.status_db_state.getCode();
        ChangeInfo changeInfo = this.createChangeInfo(info.getStatus(), redisKey, mapKey);
        changeInfo.setCollectTime(new Date());
        info.getMaps().put(mapKey, changeInfo);

        boolean flag = eventInfoChangeManagerService.infoIschange(changeInfo.getRedisKey(), changeInfo.getMapKey(), changeInfo.getValue());
        if (flag) {
            Integer status = Integer.parseInt(changeInfo.getValue().toString()) == 1 ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
            String eventRedisKey = StatusInfoChangeTypeEnum.event_db_connect.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();

            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_db_connect.getDescr(),status.equals(EventLevelEnum.NORMAL.getCode())?"正常":"异常"));
            alarmTempReq.setCollectValue( status.equals(EventLevelEnum.NORMAL.getCode())?"正常":"异常");
            alarmTempReq.setFlag("DB");
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.status_db_state.getCode(),StatusInfoChangeTypeEnum.STATUS.getCode(),"", status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq);
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_db_connect.getDescr(),status.equals(EventLevelEnum.NORMAL.getCode())?"正常":"异常"));

                this.dispatureEvent(event);
            }
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


    private ChangeInfo createChangeInfo(Object value, String redisKey, String mapKey) {
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(value);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        return changeInfo;
    }
}
