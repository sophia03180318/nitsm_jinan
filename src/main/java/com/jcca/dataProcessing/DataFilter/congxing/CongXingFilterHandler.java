package com.jcca.dataProcessing.DataFilter.congxing;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
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

/**
 * @author Zhaozheng
 * @description TODO 从兴信息变动处理类
 * @className CongxingFilterHandler
 * @date 2023/10/27 9:25
 * @since 2.1.0.0
 */
@Component("congXingFilterHandler")
public class CongXingFilterHandler extends IFilterHandler<ItsmQueueEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(ItsmQueueEntity info) {
        String redisKey = StatusInfoChangeTypeEnum.status_congxing.getCode();
        String mapKey = info.getEventId();

        boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, info.getAlarmState());
        if (flag) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(info.getNowVersion());
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey);
            changeInfo.setCollectTime(info.getAlarmTime());
            info.getMaps().put(mapKey, changeInfo);
            String eventRedisKey = StatusInfoChangeTypeEnum.event_linkQuality.getCode()+":"+info.getAlarmType();
            String eventMapKey = info.getAssetIp() + "_" + info.getAssetId() + "_" + info.getEventId();
            String msg =  info.getAlarmContent();

            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(msg);
            alarmTempReq.setCollectValue(info.getNowVersion());
            if (ObjectUtil.isNotNull(info.getOldVersion())){
                alarmTempReq.setThresholdValue(info.getOldVersion());
            }
            alarmTempReq.setFlag(mapKey);

            int status = (0==info.getAlarmState()? EventLevelEnum.ABNORMAL.getCode(): EventLevelEnum.NORMAL.getCode());
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey,status,alarmTempReq,info.getInspectRecordId(),info.getVersion());
            if (event != null) {
                //被事件信息截取
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
