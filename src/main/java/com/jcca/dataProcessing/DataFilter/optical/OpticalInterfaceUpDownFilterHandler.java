package com.jcca.dataProcessing.DataFilter.optical;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.OpticalSwitchEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Zhaozheng
 * @description TODO 光纤交换机光口状态信息过滤处理类
 * @className OpticalInterfaceUpDownFilterHandler
 * @date 2023/10/27 9:47
 * @since 2.1.0.0
 */
@Component("opticalInterfaceUpDownFilterHandler")
public class OpticalInterfaceUpDownFilterHandler extends IFilterHandler<OpticalSwitchEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;


    public static Map<String, String> moduleStateMap = new HashMap<String, String>() {{
        put("2", "没有光模块");
        put("3", "光模块故障");
        put("4", "没有光信号");
        put("5", "光信号不同步");
        put("6", "正常");
        put("7", "端口故障");
        put("8", "板卡故障");
    }};

    @Override
    public boolean handler(OpticalSwitchEntity info) {

        Map<String, String> stateMap = info.getModuleStateMap();
        if (Objects.isNull(stateMap)) {
            return true;
        }


        Set<String> list = stateMap.keySet();
        for (String item : list) {

            String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_interface.getCode() + ":" + item;
            String mapKey = StatusInfoChangeTypeEnum.status_interface_optical_status.getCode();
            String value = stateMap.get(item) + "_" + moduleStateMap.get(stateMap.get(item));
            boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, value);
            if (flag) {
                ChangeInfo changeInfo = new ChangeInfo();
                changeInfo.setValue(value);
                changeInfo.setRedisKey(redisKey);
                changeInfo.setMapKey(mapKey);
                changeInfo.setCollectTime(new Date(info.getCollectTime()));
                info.getMaps().put(mapKey, changeInfo);
                String eventRedisKey = StatusInfoChangeTypeEnum.event_port_optical_state.getCode();
                String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + item;
                //3,5,7,8判断异常
                boolean error = stateMap.get(item).equals("3") || stateMap.get(item).equals("5") || stateMap.get(item).equals("7") || stateMap.get(item).equals("8");
                Integer status = error ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();

                String str = status == EventLevelEnum.ABNORMAL.getCode() ? "异常。" : "恢复。";
                AlarmTempReq alarmTempReq = new AlarmTempReq();
                alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_port_optical_state.getDescr(), item, str));
                alarmTempReq.setCollectValue(value);
                alarmTempReq.setFlag(item);
                this.addEventStatus(StatusInfoChangeTypeEnum.event_port_optical_state.getCode(),StatusInfoChangeTypeEnum.STATUS.getCode(), item, status, info, changeInfo);
                IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId());
                if (event != null) {
                    //被事件信息截取
                    changeInfo.setIsEvent(true);
                    event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_port_optical_state.getDescr(), item, str));
                    this.dispatureEvent(event);
                }

            }
        }

        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
