package com.jcca.dataProcessing.DataFilter.station;

import com.jcca.common.utils.AppMathUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectStationSystemTimeEntity;
import com.jcca.dataProcessing.Entity.ThresholdBaseEntity;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.dataProcessing.manager.threshold.ThresholdManager;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 车站时间偏差信息过滤处理类
 * @className StationSystemTimeFilterHandler
 * @date 2023/10/27 10:01
 * @since 2.1.0.0
 */
@Component("stationSystemTimeFilterHandler")
public class StationSystemTimeFilterHandler extends IFilterHandler<CollectStationSystemTimeEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectStationSystemTimeEntity info) {
        if (Objects.isNull(info.getTimeSpan())) {
            return true;
        }
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode();
        String mapKey = StatusInfoChangeTypeEnum.status_time_deviation.getCode();

        boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey, info.getTimeSpan());
        Long timeSpan = info.getTimeSpan();
        Long timeLong = new BigDecimal(timeSpan).divide(new BigDecimal(1000), 0, BigDecimal.ROUND_HALF_UP).abs().longValue();
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(timeLong);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setIsChange(flag);
        changeInfo.setMapKey(mapKey);
        info.getMaps().put(mapKey, changeInfo);

        String eventRedisKey = StatusInfoChangeTypeEnum.event_time_state.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();
        String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_time_state.getCode(),StatusInfoChangeTypeEnum.NORMAL.getCode(),"");

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_time_state.getCode(), info.getAssetId(), null);
        if (threshold.baseValueIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey);
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(redisThresholdKey, thresholdMapKey, threshold.getBaseValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey,thresholdMapKey, threshold.getBaseValue(), info);
        }
        if (changeInfo.getIsChange() || thresholdFlag) {
            // 输出结果：采集值是否大于设定值
            Boolean compare = AppMathUtil.compare(timeLong.toString(), threshold.getBaseValue().toString());
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String keyWord= status.equals(EventLevelEnum.NORMAL.getCode())?"":"超过";
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_time_state.getDescr(), changeInfo.getValue(), keyWord,threshold.getBaseValue()));
            alarmTempReq.setCollectValue(changeInfo.getValue()+"秒");
            alarmTempReq.setThresholdValue(threshold.getBaseValue()+"秒");
            this.addEventStatus(StatusInfoChangeTypeEnum.event_time_state.getCode(),StatusInfoChangeTypeEnum.NORMAL_VAL.getCode(),"", status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq);
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);

                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_time_state.getDescr(), changeInfo.getValue(), keyWord,threshold.getBaseValue()));

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
