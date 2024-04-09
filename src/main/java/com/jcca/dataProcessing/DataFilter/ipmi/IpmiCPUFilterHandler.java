package com.jcca.dataProcessing.DataFilter.ipmi;

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
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO 管理口传感器信息过滤处理类
 * @className IpmiSensorFilterHandler
 * @date 2023/10/27 9:43
 * @since 2.1.0.0
 */
@Component("ipmiCPUFilterHandler")
public class IpmiCPUFilterHandler extends IFilterHandler<CollectSensorEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectSensorEntity info) {
        if (!SensorTypeEnum.CPU.name().equals(info.getSensorType())) {
            return true;
        }
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_CPU.getCode() + ":" + info.getName();
        String mapKey = StatusInfoChangeTypeEnum.status_cpu_value.getCode();
        boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey, info.getValue());
        if (flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getValue());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setCollectTime(new Date());
            changeInfo.setMapKey(mapKey);
            info.getMaps().put(mapKey, changeInfo);
        }

        String mapKey1 = StatusInfoChangeTypeEnum.status_cpu_status.getCode();
        boolean flag1 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey1, info.getValue());
        if (flag1) {
            Integer status = "normal".equals(info.getStatus().toLowerCase()) ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
            String str = status == EventLevelEnum.ABNORMAL.getCode() ? "异常。" : "恢复。";
            String descStr = String.format(StatusInfoChangeTypeEnum.event_cpu_state.getDescr(), info.getName(), str);

            AlarmTempReq tempReq = new AlarmTempReq();
            tempReq.setOrgMsg(descStr);
            tempReq.setCollectValue(info.getStatus());
            tempReq.setFlag(info.getName());


            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getStatus());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey1);
            changeInfo.setCollectTime(new Date());
            info.getMaps().put(mapKey1, changeInfo);
            String eventRedisKey = StatusInfoChangeTypeEnum.event_cpu_state.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getName();
            this.addEventStatus(StatusInfoChangeTypeEnum.event_cpu_state.getCode(),StatusInfoChangeTypeEnum.STATUS.getCode(),info.getName(), status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,tempReq);
            if (event != null) {
                //被事件信息截取
                event.setDescStr(descStr);
                changeInfo.setIsEvent(true);
                this.dispatureEvent(event);
            }
        }
        String mapKey2 = StatusInfoChangeTypeEnum.status_cpu_voltage.getCode();
        boolean flag2 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey2, info.getValue());
        if (flag2) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getVoltage());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey2);
            info.getMaps().put(mapKey2, changeInfo);
        }

        String mapKey3 = StatusInfoChangeTypeEnum.status_cpu_core.getCode();
        boolean flag3 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey3, info.getValue());
        if (flag3) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getVoltage());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey3);
            info.getMaps().put(mapKey3, changeInfo);
        }


        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
