package com.jcca.dataProcessing.DataFilter.commonFitler;

import com.jcca.component.casco.enums.HostRunStatusEnum;
import com.jcca.component.casco.enums.LinkStatusEnum;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.ItsmQueueEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO casco软件连接过滤处理类(支持铁科、北洋软件连接)
 * @className CascoLinkFitlerHandler
 * @date 2023/10/27 11:24
 * @since 2.1.0.0
 */
@Slf4j
@Component("softLinkFitlerHandler")
public class SoftLinkFitlerHandler extends IFilterHandler<ItsmQueueEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(ItsmQueueEntity info) {

        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_softLinkState.getCode();
        String mapKey = info.getEntityId() + "_" + info.getAbFlag() + "_" + info.getIdStr();

        boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, info.getLinkStatus());
        if (flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getLinkStatus());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(new Date());
            info.getMaps().put(mapKey, changeInfo);
            String eventRedisKey = StatusInfoChangeTypeEnum.event_CTC_link.getCode();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getIdStr();
            Integer status = info.getLinkStatus().equals("UP") ? EventLevelEnum.NORMAL.getCode() : EventLevelEnum.ABNORMAL.getCode();

            String msg = "";
            //北洋、铁科软件的连接有collectValue描述信息
            if (info.getCollectValue() != null && !"".equals(info.getCollectValue())) {
                msg = info.getCollectValue();
            } else {
                msg = String.format(StatusInfoChangeTypeEnum.event_CTC_link.getDescr(), info.getAssetIp());
            }

            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(msg);
            alarmTempReq.setCollectValue(status.toString());
            alarmTempReq.setFlag(mapKey);
            this.addEventStatus(StatusInfoChangeTypeEnum.event_CTC_link.getCode(), StatusInfoChangeTypeEnum.LINK_STATUS.getCode(), mapKey, status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId());

            if (event != null) {
                changeInfo.setIsEvent(true);
                event.setDescLog(msg);


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
