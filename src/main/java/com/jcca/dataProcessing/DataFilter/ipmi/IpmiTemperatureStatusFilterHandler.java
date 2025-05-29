package com.jcca.dataProcessing.DataFilter.ipmi;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectSensorEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.enums.SensorTypeEnum;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @description: 温度状态
 * @author:
 * @create: 2023/11/07 10:05
 */
@Component("ipmiTemperatureStatusFilterHandler")
public class IpmiTemperatureStatusFilterHandler extends IFilterHandler<CollectSensorEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;


    @Override
    public boolean handler(CollectSensorEntity info) {
        if (!SensorTypeEnum.GAUGE.name().equals(info.getSensorType())) {
            return true;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "管理口温度状态过滤处理类", info.getAssetIp());
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_temp.getCode() + ":" + info.getName();
        String mapKey1 = StatusInfoChangeTypeEnum.status_tempStatus.getCode();
        boolean flag1 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey1, info.getStatus());
        if (flag1) {
            List<String> normalList = Arrays.asList("normal", "1", "0");
            Integer status = normalList.contains(info.getStatus().toLowerCase()) ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
            String str = status == EventLevelEnum.ABNORMAL.getCode() ? "异常。" : "恢复。";
            String descStr = String.format(StatusInfoChangeTypeEnum.event_temp_state.getDescr(), info.getName(), str);

            AlarmTempReq tempReq = new AlarmTempReq();
            tempReq.setOrgMsg(descStr);
            tempReq.setCollectValue(info.getStatus());
            tempReq.setFlag(info.getName());

            ChangeInfo changeInfo2 = new ChangeInfo();
            changeInfo2.setValue(info.getStatus());
            changeInfo2.setRedisKey(redisKey);
            changeInfo2.setMapKey(mapKey1);
            changeInfo2.setCollectTime(new Date());
            info.getMaps().put(mapKey1, changeInfo2);
            String eventRedisKey2 = StatusInfoChangeTypeEnum.event_temp_state.getCode();
            String eventMapKey2 = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getName();
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_temp_state.getCode(), StatusInfoChangeTypeEnum.STATUS.getCode(), info.getSerialNumberName(), status, info, changeInfo2);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo2, eventRedisKey2, eventMapKey2, status,tempReq);
            if (event != null) {
                //被事件信息截取
                changeInfo2.setIsEvent(true);
                event.setDescStr(descStr);
                this.dispatureEvent(event);
            }
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
