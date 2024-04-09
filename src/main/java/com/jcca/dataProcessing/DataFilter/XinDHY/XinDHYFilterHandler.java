package com.jcca.dataProcessing.DataFilter.XinDHY;

import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.ItsmQueueEntity;
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
 * @description TODO 信达环宇信息过滤处理类
 * @className XinDHYFilterHandler
 * @date 2023/10/27 10:05
 * @since 2.1.0.0
 */
@Component("xinDHYFilterHandler")
public class XinDHYFilterHandler extends IFilterHandler<ItsmQueueEntity> {
    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(ItsmQueueEntity info) {
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status_xdhy.getCode();
        String mapKey = info.getIdStr();


        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(info.getCollectValue());
        changeInfo.setCollectTime(new Date());
        changeInfo.setRedisKey(redisKey);
        changeInfo.setMapKey(mapKey);
        info.getMaps().put(mapKey, changeInfo);
        String eventRedisKey = StatusInfoChangeTypeEnum.event_xdhy_state.getCode();
        String eventMapKey = mapKey;
        AlarmTempReq alarmTempReq = new AlarmTempReq();
        alarmTempReq.setOrgMsg(info.getAlarmContent());
        alarmTempReq.setCollectValue(changeInfo.getValue().toString());

        int status = (0==info.getAlarmState()? EventLevelEnum.ABNORMAL.getCode(): EventLevelEnum.NORMAL.getCode());

        this.addEventStatus(StatusInfoChangeTypeEnum.event_xdhy_state.getCode(),StatusInfoChangeTypeEnum.STATUS.getCode(),"", status, info, changeInfo);
        IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq);
        if (event != null) {
            //被事件信息截取
            changeInfo.setIsEvent(true);
            event.setDescLog(info.getAlarmContent());
            this.dispatureEvent(event);
        }
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
