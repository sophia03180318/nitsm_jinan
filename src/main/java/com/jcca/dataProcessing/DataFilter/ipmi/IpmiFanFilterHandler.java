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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO 管理口传感器信息过滤处理类
 * @className IpmiSensorFilterHandler
 * @date 2023/10/27 9:43
 * @since 2.1.0.0
 */
@Component("ipmiFanFilterHandler")
public class IpmiFanFilterHandler extends IFilterHandler<CollectSensorEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(CollectSensorEntity info) {
        if (!SensorTypeEnum.FAN.name().equals(info.getSensorType())) {
            return true;
        }
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_fan.getCode() + ":" + info.getName();
        String mapKey = StatusInfoChangeTypeEnum.status_fanvalue.getCode();
        boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey, info.getValue());
        if (flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getValue());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(new Date());
            info.getMaps().put(mapKey, changeInfo);
        }

        String mapKey1 = StatusInfoChangeTypeEnum.status_fanStatus.getCode();
        boolean flag1 = eventInfoChangeManagerService.infoIschange(redisKey, mapKey1, info.getValue());
        if (flag1) {
            List<String> normalList = Arrays.asList("normal", "1", "0");
            Integer status = normalList.contains(info.getStatus().toLowerCase()) ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();
            String str = status == EventLevelEnum.ABNORMAL.getCode() ? "异常。" : "恢复。";

            String descStr = String.format(StatusInfoChangeTypeEnum.event_fan_state.getDescr(), info.getName(), str);

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
            String eventRedisKey = StatusInfoChangeTypeEnum.event_fan_state.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getName();
            //添加状态监控（设备监控的事件信息是否正常）
            this.addEventStatus(StatusInfoChangeTypeEnum.event_fan_state.getCode(),StatusInfoChangeTypeEnum.STATUS.getCode(), info.getName(), status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,tempReq);
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
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
