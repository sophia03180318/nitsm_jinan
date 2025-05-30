package com.jcca.dataProcessing.DataFilter.port;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectPortUsedNumberEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO 端口使用数量过滤处理类
 * @className PortNumberFilterHandler
 * @date 2023/10/27 9:56
 * @since 2.1.0.0
 */
@Component("portNumberTcpFilterHandler")
public class PortNumberTcpFilterHandler extends IFilterHandler<CollectPortUsedNumberEntity> {
    private  final String SERVER_MAX_PORT = "65535";
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;


    @Override
    public boolean handler(CollectPortUsedNumberEntity info) {
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode();
        String mapKey1 = StatusInfoChangeTypeEnum.status_tcp_portNumber.getCode();
        String mapKey2 = StatusInfoChangeTypeEnum.status_tcp_max_port.getCode();

        List<String> tcpPortList = info.getTcpPortList();
        Collections.sort(tcpPortList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.valueOf(o1) > Integer.valueOf(o2) ? -1 : 1;
            }
        });

        if(tcpPortList.isEmpty()){
            return true;
        }

        int portNum = tcpPortList.size();
        String maxPort = tcpPortList.get(0);
        boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey1, portNum);
        if (flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(portNum);
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey1);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey1, changeInfo);
        }

        boolean flag1 = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey2, maxPort);
        if (flag1) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(maxPort);
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey2);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            info.getMaps().put(mapKey2, changeInfo);
            Integer status = maxPort.equals(SERVER_MAX_PORT) ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String eventRedisKey = StatusInfoChangeTypeEnum.event_system_tcpPort_status.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_system_tcpPort_status.getDescr(), maxPort));
            alarmTempReq.setCollectValue(changeInfo.getValue().toString());
            this.addEventStatus(StatusInfoChangeTypeEnum.event_system_tcpPort_status.getCode(), StatusInfoChangeTypeEnum.TCP_STATUS.getCode(),"", status,info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_system_tcpPort_status.getDescr(), maxPort));
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
