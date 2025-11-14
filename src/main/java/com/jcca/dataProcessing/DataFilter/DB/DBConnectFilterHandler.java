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
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 数据库连接状态过滤处理类
 * @className DBConnectFilterHandler
 * @date 2023/10/27 9:30
 * @since 2.1.0.0
 */
@Component("dBConnectFilterHandler")
public class DBConnectFilterHandler extends IFilterHandler<CollectDBEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectDBEntity info) {
        ChangeInfo changeInfo = info.getMaps().get(StatusInfoChangeTypeEnum.status_db_state.getCode());

        boolean flag= eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),changeInfo.getRedisKey(), changeInfo.getMapKey(),changeInfo.getValue());
        if(flag && Objects.nonNull(changeInfo.getValue())){
            Integer status = changeInfo.getValue().toString().toLowerCase().equals("open") ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
            String eventRedisKey = StatusInfoChangeTypeEnum.event_db_connect.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();

            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_db_connect.getDescr(),status.equals(EventLevelEnum.NORMAL.getCode())?"正常":"异常"));
            alarmTempReq.setCollectValue( status.equals(EventLevelEnum.NORMAL.getCode())?"正常":"异常");
            alarmTempReq.setFlag("DB");
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_db_connect.getCode(),StatusInfoChangeTypeEnum.DB_CONN.getCode(),"", status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId(),info.getVersion());
            if (event != null) {
                //被事件信息截取
                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_db_connect.getDescr(),status.equals(EventLevelEnum.NORMAL.getCode())?"正常":"异常"));
                changeInfo.setIsEvent(true);
                this.dispatureEvent(event);
            }
        }
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
