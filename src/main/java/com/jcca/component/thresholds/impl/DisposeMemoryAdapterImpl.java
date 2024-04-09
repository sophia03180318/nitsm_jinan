package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.bean.constant.RedisCacheConst;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppMathUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectMemoryBean;
import com.jcca.component.thresholds.bean.CollectProcessBean;
import com.jcca.web.asset.enums.ThresholdSectionEnum;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.service.bean.VerifyThresholdReq;
import com.jcca.web.asset.service.bean.VerifyThresholdResp;
import com.jcca.web.asset.service.bean.VerifyThresholdSectionResp;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.service.CollectMemoryService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 处理运行内存
 *
 * @author Lvyp
 */
@Slf4j
@Component
public class DisposeMemoryAdapterImpl implements CollectAdapter {

    @Resource
    private CollectMemoryService memService;
    @Resource
    private ThresholdAssetService thresholdServ;
    @Resource
    private EventLogicService eventLogicServ;
    @Resource
    private RedisService redisService;

    /**
     * 处理MEM采集数据
     *
     * @param data
     */
    @Override
    public void dispose(JSONArray data) {
        List<CollectMemoryBean> memList = JSONUtil.toList(data, CollectMemoryBean.class);
        List<CollectMemory> dataMemList = new ArrayList<CollectMemory>();
        String collectCode = MyIdUtil.getId();

        boolean top5 = true;
        for (CollectMemoryBean item : memList) {
            if (StrUtil.isEmpty(item.getAssetId()) || StrUtil.isEmpty(item.getCollectTime())
                    || Objects.isNull(item.getMemTotal()) || Objects.isNull(item.getMemUsed())
                    || Objects.isNull(item.getSwapTotal()) || Objects.isNull(item.getSwapUsed())) {
                log.error("【接收到验证未通过的内存采集数据】：" + JSONUtil.toJsonStr(item));
                continue;
            }
            // 进程内存占用率较大前5
            List<CollectProcessBean> processTop5List = item.getProcessTop5List();
            if (top5 && Objects.nonNull(processTop5List)) {
                for (CollectProcessBean ben : processTop5List) {
                    String collectTime = ben.getCollectTime();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String format = formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(collectTime)), ZoneId.systemDefault()));
                    ben.setCollectTime(format);
                }
                redisService.set(RedisCacheConst.TOP5_PROCESS_MEM + item.getAssetId(), JSONUtil.toJsonStr(processTop5List));
                top5 = false;
            }

            Date date = new Date();
            date.setTime(Long.parseLong(item.getCollectTime()));

            CollectMemory mem = EntityBeanUtil.copy(item, CollectMemory.class);
            double memUsedRate = AppMathUtil.percentageDouble(item.getMemUsed(), item.getMemTotal(), 2);
            double swapUsedRate = AppMathUtil.percentageDouble(item.getSwapUsed(), item.getSwapTotal(), 2);
            mem.setSwapUsedRate(swapUsedRate);
            mem.setMemUsedRate(memUsedRate);
            mem.setCollectCode(collectCode);
            mem.setCollectTime(date);
            mem.setId(MyIdUtil.getId());
            dataMemList.add(mem);

            // 处理事件
            addEvent(mem);
        }
        if (dataMemList.isEmpty()) {
            return;
        }
        // 保存
        memService.saveBatch(dataMemList);
        // 刷新实时缓存
        memService.updateRealTimeData(dataMemList);

    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.MEM;
    }

    /**
     * 触发事件
     *
     * @param item
     */
    private void addEvent(CollectMemory item) {
        String assetId = item.getAssetId();
        synchronized (assetId.intern()) {
            VerifyThresholdReq req = new VerifyThresholdReq();
            req.setCollectValue(item.getMemUsedRate().toString());
            req.setAssetId(assetId);
            req.setHaveSection(true);
            req.setType(ThresholdSectionEnum.MENORY);
            VerifyThresholdResp verifyResp = thresholdServ.verifyThreshold(req);

            List<CreateEventReq> eventList = new ArrayList<CreateEventReq>();
            if (Objects.nonNull(verifyResp)) {
                CreateEventReq eventReq = new CreateEventReq();
                eventReq.setOriginalMsg(verifyResp.getMsg());
                eventReq.setEventLevel(verifyResp.getAlarmStatus() ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode());

                eventReq.setAssetId(assetId);
                eventReq.setBaseValue(verifyResp.getBaseValue());
                eventReq.setCollectValue(item.getMemUsedRate().toString());
                eventReq.setUniqueCode(EventUniqueCode.MEMORY_UNIQUE_CODE);
                eventReq.setFlag("内存");
                eventReq.setGroupFlag(EventGroupConstant.MEMORY);
                eventReq.setCreateTime(item.getCollectTime());
                eventList.add(eventReq);
            }

            //区间阈值
            VerifyThresholdSectionResp thresholdSectionResp = thresholdServ.verifySectionThreshold(Double.valueOf(item.getMemUsedRate()), EventUniqueCode.MEMORY_UNIQUE_CODE);
            List<CreateEventReq> memoryEventList = thresholdServ.disposeVerifyThresholdSectionResp(thresholdSectionResp, EventGroupConstant.MEMORY, item.getCollectTime(), assetId, item.getMemUsedRate().toString(), "内存阈值");
            eventList.addAll(memoryEventList);

            try {
                for (CreateEventReq createEventReq : eventList) {
                    eventLogicServ.addEvent(createEventReq);
                }
            } catch (Exception e) {
                log.error("处理内存事件异常：{}", e.getMessage(), e);
            }
        }

    }

}
