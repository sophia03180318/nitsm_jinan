package com.jcca.dataProcessing.DataFilter.mq;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.MQMonitorEntity;
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
 * @description TODO
 * @className MQstatusFilterHandler
 * @date 2024/1/6 10:37
 * @since 2.1.0.0
 */
@Component("mqstatusFilterHandler")
public class MQstatusFilterHandler extends IFilterHandler<MQMonitorEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(MQMonitorEntity info) {
        //name是否为空来判断是否为mq的状态
        if (info.getName() != null) {
            return true;
        }
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode();
        String mapKey = StatusInfoChangeTypeEnum.status_mq.getCode();

        //判断数据是否有变化
        ChangeInfo changeInfo = null;
        boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey, info.getStatus());
        if (flag) {
            changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getStatus());
            changeInfo.setIsChange(true);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            info.getMaps().put(mapKey, changeInfo);
        }


        String eventRedisKey = StatusInfoChangeTypeEnum.event_mq_state.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();

        if (flag) {
            //状态信息有变化
            Integer status = info.getStatus() == EventLevelEnum.ABNORMAL.getCode() ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String str = status == EventLevelEnum.ABNORMAL.getCode() ? "中断。" : "恢复连接。";
            String descStr = String.format(StatusInfoChangeTypeEnum.event_mq_state.getDescr(), str);

            AlarmTempReq tempReq = new AlarmTempReq();
            tempReq.setOrgMsg(descStr);
            tempReq.setCollectValue(info.getStatus().toString());
            tempReq.setFlag(info.getName());

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
