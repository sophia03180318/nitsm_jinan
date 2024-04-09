package com.jcca.component.thresholds.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectStationSystemTimeBean;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.service.bean.VerifyThresholdSectionResp;
import com.jcca.web.asset.vo.ThresholdAssetVo;
import com.jcca.web.collect.entity.CollectSystemTime;
import com.jcca.web.collect.service.CollectSystemTimeService;
import com.jcca.web.event.enums.EventLevelEnum;
import com.jcca.web.xunjian.controller.util.XunjianReportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 车站时间处理
 *
 * @author Lvyp
 */
@Slf4j
@Component
public class DisposeStationSystemTimeAdapterImpl implements CollectAdapter {

    @Resource
    private CollectSystemTimeService systemTimeService;
    @Resource
    private ThresholdAssetService thresholdServ;
    @Resource
    private EventLogicService eventLogicServ;
    @Resource
    private AssetService assetService;

    @Override
    public void dispose(JSONArray data) {
        log.debug("接收到车站采集的时间数据：" + JSONUtil.toJsonStr(data));
        List<CollectStationSystemTimeBean> timeList = JSONUtil.toList(data, CollectStationSystemTimeBean.class);
        log.debug("处理车站采集的时间数据：" + JSONUtil.toJsonStr(timeList));
        List<CollectSystemTime> sysTimeList = new ArrayList<CollectSystemTime>();
        for (CollectStationSystemTimeBean item : timeList) {
            String assetId = item.getAssetId();
            String timeSpan = item.getTimeSpan();
            String collectTime = item.getCollectTime();
            boolean empty = StrUtil.isEmpty(assetId);
            boolean empty3 = StrUtil.isEmpty(collectTime);
            boolean empty2 = StrUtil.isEmpty(timeSpan);
            boolean isNOtNumber = !NumberUtil.isNumber(timeSpan);
            if (empty || empty3) {
                continue;
            }
            Date date = new Date();
            date.setTime(Long.valueOf(item.getCollectTime()));

            CollectSystemTime systime = new CollectSystemTime();
            systime.setId(MyIdUtil.getId());
            systime.setAssetId(assetId);
            systime.setCollectTime(date);
            systime.setCreateTime(new Date());
            systime.setTimeduration(item.getTimeduration());
            if (StrUtil.isNotEmpty(item.getRemoteTime())) {
                systime.setSystemDate(DateUtil.parse(item.getRemoteTime(), DatePattern.NORM_DATETIME_MS_PATTERN));
            }

            if (!isNOtNumber || !empty2) {
                systime.setTimeSpan(Long.valueOf(timeSpan));
                addEvent(systime);
            }


            if (ObjectUtil.isNotNull(systime.getTimeduration())) {
                //F
                addRunningTimeEvent(systime);
                //重启事件  运行时长小于半小时
                if (0 < systime.getTimeduration() && systime.getTimeduration() < 1800) {
                    List<CollectSystemTime> realTimeData = systemTimeService.getRealTimeData(item.getAssetId());
                    if (Objects.nonNull(realTimeData) && !realTimeData.isEmpty()) {
                        Long timeduration = realTimeData.get(0).getTimeduration();
                        Asset asset = assetService.getById(item.getAssetId());
                        if (Objects.nonNull(asset) && Objects.nonNull(asset.getCollectionType()) && asset.getCollectionType() == 1) {
                            if (Objects.nonNull(timeduration) && timeduration <= 42940800) {
                                addRestartEvent(systime, timeduration);
                            }
                        } else {
                            if (Objects.nonNull(timeduration)) {
                                addRestartEvent(systime, timeduration);
                            }
                        }
                    }

                }
            }

            sysTimeList.add(systime);
        }
        if (sysTimeList.isEmpty()) {
            return;
        }
        // 刷新缓存
        systemTimeService.updateRealTimeData(sysTimeList);
        // 更新全部
        systemTimeService.updateBatchByAssetId(sysTimeList);
    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.SYSTEM_STATION_TIME;
    }

    /**
     * 添加事件
     */
    void addEvent(CollectSystemTime item) {
        String assetId = item.getAssetId();
        synchronized (assetId.intern()) {
            ThresholdAssetVo threshold = thresholdServ.findAssetThreshold(assetId);
            if (Objects.isNull(threshold) || Objects.isNull(threshold.getTimeDeviation())) {
                threshold = new ThresholdAssetVo();
                threshold.setTimeDeviation(999999999);
            }

            String baseValue = new BigDecimal(threshold.getTimeDeviation()).multiply(new BigDecimal(1000)).longValue()
                    + "";

            BigDecimal timeSpanabs = new BigDecimal(item.getTimeSpan()).abs();
            Long collectValue = timeSpanabs.longValue();

            // 输出结果：采集值是否大于设定值
            Boolean compare = AppMathUtil.compare(collectValue.toString(), baseValue);

            CreateEventReq eventReq = new CreateEventReq();

            long timeLong = timeSpanabs.divide(new BigDecimal(1000), 0, BigDecimal.ROUND_HALF_UP).longValue();
            if (compare) {
                eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                String format = String.format("时间偏差采集值：%s 秒,设定值：%s 秒，超阈值！",
                        timeLong,
                        threshold.getTimeDeviation());
                eventReq.setOriginalMsg(format);
            } else {
                eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                String format = String.format("时间偏差采集值：%s 秒,设定值：%s 秒,指标正常！",
                        timeLong,
                        threshold.getTimeDeviation());
                eventReq.setOriginalMsg(format);
            }

            eventReq.setCreateTime(item.getCollectTime());
            eventReq.setAssetId(assetId);
            eventReq.setBaseValue(baseValue);
            eventReq.setCollectValue(timeLong + "");
            eventReq.setUniqueCode(EventUniqueCode.THRE_SYSTIME_UNIQUE_CODE);
            eventReq.setFlag("时间偏差");
            eventReq.setGroupFlag(EventGroupConstant.SYSTIME_TIME);

            try {
                eventLogicServ.addEvent(eventReq);
            } catch (Exception e) {
                log.error("处理时间偏差事件异常：{}", e.getMessage(), e);
            }
        }
    }

    /**
     * 添加运行时长事件
     */
    void addRunningTimeEvent(CollectSystemTime item) {
        String assetId = item.getAssetId();
        synchronized (assetId.intern()) {
            ThresholdAssetVo threshold = thresholdServ.findAssetThreshold(assetId);
            if (Objects.isNull(threshold) || Objects.isNull(threshold.getRunningTimeDeviation())) {
                threshold = new ThresholdAssetVo();
                threshold.setRunningTimeDeviation(999999999);
            }
            //运行时长(天) 设定阈值
            String baseValue = threshold.getRunningTimeDeviation() + "";

//
            BigDecimal collectSecond = new BigDecimal(item.getTimeduration()).divide(new BigDecimal(86400), 0, BigDecimal.ROUND_DOWN);
            Long collectValue = collectSecond.longValue();

            // 输出结果：采集值是否大于设定值
            Boolean compare = AppMathUtil.compare(collectValue.toString(), baseValue);


            List<CreateEventReq> eventReqList = new ArrayList<CreateEventReq>();

            CreateEventReq eventReq = new CreateEventReq();
            if (compare) {
                eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                String format = String.format("运行时长：%s 天,设定值：%s 天,超阈值！",
                        collectValue,
                        baseValue);
                eventReq.setOriginalMsg(format);
            } else {
                eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                String format = String.format("运行时长：%s 天,设定值：%s 天,指标正常！",
                        collectValue,
                        baseValue);
                eventReq.setOriginalMsg(format);
            }

            eventReq.setAssetId(assetId);
            eventReq.setCreateTime(item.getCollectTime());
            eventReq.setBaseValue(threshold.getRunningTimeDeviation().toString());
            eventReq.setCollectValue(collectValue + "");
            eventReq.setUniqueCode(EventUniqueCode.THRE_RUNNINGTIME_UNIQUE_CODE);
            eventReq.setGroupFlag(EventGroupConstant.RUNNING_TIME);
            eventReq.setFlag("运行时长");

            //阶段阈值
            VerifyThresholdSectionResp thresholdSectionResp = thresholdServ.verifySectionThreshold(collectValue.doubleValue(), EventUniqueCode.THRE_RUNNINGTIME_UNIQUE_CODE);
            List<CreateEventReq> diskEventList = thresholdServ.disposeVerifyThresholdSectionResp(thresholdSectionResp, EventGroupConstant.RUNNING_TIME, item.getCollectTime(), assetId, collectValue.toString(), "运行时长阈值");

            eventReqList.add(eventReq);
            eventReqList.addAll(diskEventList);

            try {
                for (CreateEventReq createEventReq : eventReqList) {
                    eventLogicServ.addEvent(createEventReq);
                }
            } catch (Exception e) {
                log.error("处理运行时长事件异常：{}", e.getMessage(), e);
            }
        }
    }


    /**
     * 添加重启事件
     */
    void addRestartEvent(CollectSystemTime item, Long timeduration) {
        String assetId = item.getAssetId();
        synchronized (assetId.intern()) {
            Long collectValue = item.getTimeduration();
            if (collectValue < timeduration) {
                String timeStr = XunjianReportUtil.formatDateTime(collectValue);
                CreateEventReq eventReq = new CreateEventReq();
                eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                String format = "检测到设备可能存在重启情况,请检查设备运行状态是否正常。";
                eventReq.setOriginalMsg(format);
                eventReq.setAssetId(assetId);
                eventReq.setCreateTime(item.getCollectTime());
                eventReq.setBaseValue(timeStr);
                eventReq.setCollectValue(collectValue + "");
                eventReq.setUniqueCode(EventUniqueCode.RESTART_UNIQUE_CODE);
                eventReq.setGroupFlag(EventGroupConstant.RESTART);
                eventReq.setFlag("设备重启");
                try {
                    eventLogicServ.addEvent(eventReq);
                } catch (Exception e) {
                    log.error("处理重启事件异常：{}", e.getMessage(), e);
                }
            }
        }
    }

}
