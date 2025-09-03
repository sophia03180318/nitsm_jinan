package com.jcca.dataProcessing.DataFilter.centerSystem;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CollectSystemTimeEntity;
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
import java.util.Date;
import java.util.Objects;

/**
 * @author Zhaozheng
 * @description TODO 中心设备时间偏差信息过滤处理类
 * @className CenterSystemTimeFilterHandler
 * @date 2023/10/27 10:01
 * @since 2.1.0.0
 */
@Component("centerSystemTimeFilterHandler")
public class CenterSystemTimeFilterHandler extends IFilterHandler<CollectSystemTimeEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectSystemTimeEntity info) {
        Long timeSpan = info.getTimeSpan();
        if (timeSpan == null) {
            return true;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "中心设备时间偏差信息过滤处理类", info.getAssetIp());
        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode();

        BigDecimal timeSpanAbs = new BigDecimal(timeSpan).abs();
        Long timeLong = timeSpanAbs.divide(new BigDecimal(1000), 0, BigDecimal.ROUND_HALF_UP).longValue();
        String mapKey0 = StatusInfoChangeTypeEnum.status_system_time.getCode();
        if (info.getSystemDate() != null) {
            ChangeInfo changeInfo = new ChangeInfo();
            changeInfo.setValue(timeLong);
            changeInfo.setCollectTime(new Date(info.getCollectTime()));
            changeInfo.setRedisKey(redisKey);
            changeInfo.setMapKey(mapKey0);
            info.getMaps().put(mapKey0, changeInfo);
        }


        if (Objects.isNull(info.getTimeSpan())) {
            return true;
        }
        String mapKey = StatusInfoChangeTypeEnum.status_time_deviation.getCode();
        boolean flag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisKey, mapKey, timeLong);
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(timeLong);
        changeInfo.setRedisKey(redisKey);
        changeInfo.setIsChange(flag);
        changeInfo.setMapKey(mapKey);
        changeInfo.setCollectTime(new Date(info.getCollectTime()));
        info.getMaps().put(mapKey, changeInfo);

        String eventRedisKey = StatusInfoChangeTypeEnum.event_time_state.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();
        String redisThresholdKey =thresholdManager.getThresholdRedisKey(info.getAssetId(),info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_time_state.getCode(),StatusInfoChangeTypeEnum.NORMAL.getCode(),"");

        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_time_state.getCode(), info.getAssetId(), null);
        if (threshold.baseValueIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey,info.getInspectRecordId());
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(info.getInspectRecordId(),redisThresholdKey, thresholdMapKey, threshold.getBaseValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey,thresholdMapKey, threshold.getBaseValue(), info);;
        }
        //如果性能数据有变动或者阈值有变动,则需要重新推送事件信息
        if (changeInfo.getIsChange() || thresholdFlag) {
            // 输出结果：采集值是否大于设定值
            Boolean compare = AppMathUtil.compare(timeLong.toString(), threshold.getBaseValue().toString());

            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String keyWord = status.equals(EventLevelEnum.NORMAL.getCode()) ? "" : "超过";
            //添加状态监控（设备监控的事件信息是否正常）
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_time_state.getDescr(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));
            alarmTempReq.setCollectValue(timeLong+"秒");
            alarmTempReq.setThresholdValue(threshold.getBaseValue() + "秒");
            this.addEventStatus(StatusInfoChangeTypeEnum.event_time_state.getCode(),StatusInfoChangeTypeEnum.NORMAL_VAL.getCode(),"", status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status,alarmTempReq,info.getInspectRecordId());
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);

                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_time_state.getDescr(), changeInfo.getValue(), keyWord, threshold.getBaseValue()));
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
