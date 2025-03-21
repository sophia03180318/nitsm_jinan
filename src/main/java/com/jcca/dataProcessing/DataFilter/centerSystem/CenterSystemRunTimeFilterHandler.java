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
 * @description TODO 中心运行时长信息过滤处理类
 * @className CenterSystemRunTimeFilterHandler
 * @date 2023/10/27 10:01
 * @since 2.1.0.0
 */
@Component("centerSystemRunTimeFilterHandler")
public class CenterSystemRunTimeFilterHandler extends IFilterHandler<CollectSystemTimeEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;
    @Resource
    private ThresholdManager thresholdManager;

    @Override
    public boolean handler(CollectSystemTimeEntity info) {
        if (Objects.isNull(info.getTimeduration())) {
            return true;
        }
        AppLogUtils.buildLogInfo(LogFunctionEnum.DATA_PROCESS_SINGLE, "中心运行时长信息过滤处理类", info.getAssetIp());
        BigDecimal collectDay = new BigDecimal(info.getTimeduration()).divide(new BigDecimal(86400), 0, BigDecimal.ROUND_DOWN);

        String redisKey = info.getAssetIp() + ":" + info.getAssetId() + ":" + StatusInfoChangeTypeEnum.status.getCode();
        String mapKey = StatusInfoChangeTypeEnum.status_run_time.getCode();

        String mapKeyRestart = StatusInfoChangeTypeEnum.status_run_time_restart.getCode();
        boolean flagRestart = eventInfoChangeManagerService.infoIschange(redisKey, mapKeyRestart, info.getTimeduration());
        ChangeInfo changeInfo2 = new ChangeInfo();
        changeInfo2.setValue(info.getTimeduration());
        changeInfo2.setRedisKey(redisKey);
        changeInfo2.setCollectTime(new Date(info.getCollectTime()));
        changeInfo2.setMapKey(mapKeyRestart);

        info.getMaps().put(mapKeyRestart, changeInfo2);


        //判断数据是否有变化
        boolean flag = eventInfoChangeManagerService.infoIschange(redisKey, mapKey, info.getTimeduration());
        ChangeInfo changeInfo = new ChangeInfo();
        changeInfo.setValue(info.getTimeduration());
        changeInfo.setRedisKey(redisKey);
        changeInfo.setCollectTime(new Date(info.getCollectTime()));
        changeInfo.setMapKey(mapKey);

        info.getMaps().put(mapKey, changeInfo);

        //阈值配置
        String eventRedisKey = StatusInfoChangeTypeEnum.event_run_time_state.getCode();
        String eventMapKey = info.getAssetIp() + "_" + info.getAssetId();
        String redisThresholdKey = thresholdManager.getThresholdRedisKey(info.getAssetId(), info.getAssetIp());
        String thresholdMapKey = thresholdManager.getThresholdMapKey(StatusInfoChangeTypeEnum.event_run_time_state.getCode(), StatusInfoChangeTypeEnum.NORMAL.getCode(), "");


        ThresholdBaseEntity threshold = thresholdManager.getThresholdValue(StatusInfoChangeTypeEnum.event_run_time_state.getCode(), info.getAssetId(), null);
        if (threshold.baseValueIsNull()) {
            IEvent event = eventInfoChangeManagerService.creatRecoveryThresholdEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, redisThresholdKey, thresholdMapKey);
            if (event != null) {
                this.dispatureEvent(event);
            }
            return true;
        }

        boolean thresholdFlag = eventInfoChangeManagerService.infoIschange(redisThresholdKey, thresholdMapKey, threshold.getBaseValue());
        if (thresholdFlag) {
            this.addThresholdStatus(redisThresholdKey, thresholdMapKey, threshold.getBaseValue(), info);
        }
        //如果性能数据有变动或者阈值有变动,则需要重新推送事件信息
        if (flag || thresholdFlag) {
            // 输出结果：采集值是否大于设定值
            Boolean compare = AppMathUtil.compare(collectDay.toString(), threshold.getBaseValue().toString());
            Integer status = compare ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode();
            String keyWord = status.equals(EventLevelEnum.NORMAL.getCode()) ? "" : "超过";
            //添加状态监控（设备监控的事件信息是否正常）
            AlarmTempReq alarmTempReq = new AlarmTempReq();
            alarmTempReq.setOrgMsg(String.format(StatusInfoChangeTypeEnum.event_run_time_state.getDescr(), collectDay, keyWord, threshold.getBaseValue()));
            alarmTempReq.setCollectValue(collectDay + "天");
            alarmTempReq.setThresholdValue(threshold.getBaseValue() + "天");
            this.addEventStatus(StatusInfoChangeTypeEnum.event_run_time_state.getCode(), StatusInfoChangeTypeEnum.NORMAL_VAL.getCode(), null, status, info, changeInfo);
            IEvent event = eventInfoChangeManagerService.creatChangeEvent(info.getAssetId(), changeInfo, eventRedisKey, eventMapKey, status, alarmTempReq);
            if (event != null) {
                //被事件信息截取
                changeInfo.setIsEvent(true);
                event.setDescStr(String.format(StatusInfoChangeTypeEnum.event_run_time_state.getDescr(), collectDay, keyWord, threshold.getBaseValue()));
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
