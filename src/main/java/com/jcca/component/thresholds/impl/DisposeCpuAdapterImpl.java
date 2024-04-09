package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.bean.constant.RedisCacheConst;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.AppPattenUtils;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectCpuBean;
import com.jcca.component.thresholds.bean.CollectProcessBean;
import com.jcca.web.asset.enums.ThresholdSectionEnum;
import com.jcca.web.asset.service.ThresholdAssetService;
import com.jcca.web.asset.service.bean.VerifyThresholdReq;
import com.jcca.web.asset.service.bean.VerifyThresholdResp;
import com.jcca.web.asset.service.bean.VerifyThresholdSectionResp;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.service.CollectCpuService;
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
 * cpu采集数据处理
 *
 * @author Lvyp
 */
@Slf4j
@Component
public class DisposeCpuAdapterImpl implements CollectAdapter {


    @Resource
    private CollectCpuService cpuService;
    @Resource
    private ThresholdAssetService thresholdServ;
    @Resource
    private RedisService redisService;
    @Resource
    private EventLogicService eventLogicServ;

    /**
     * cpu 数据处理
     *
     * @param data
     */

    @Override
    public void dispose(JSONArray data) {
        List<CollectCpuBean> cpus = JSONUtil.toList(data, CollectCpuBean.class);
        String collectCode = MyIdUtil.getId();
        List<CollectCpu> cpuList = new ArrayList<CollectCpu>();

        boolean top5 = true;
        for (CollectCpuBean item : cpus) {
            if (StrUtil.isEmpty(item.getAssetId()) || StrUtil.isEmpty(item.getCollectTime())
                    || StrUtil.isEmpty(item.getCpuFlg())) {
                log.error("【接收到验证未通过的CPU采集数据】：" + JSONUtil.toJsonStr(item));
                continue;
            }
            // 校验格式
            if (!AppPattenUtils.isDouble(item.getCpuUsedRate())) {
                log.error("【接收到验证未通过的CPU采集数据】：" + JSONUtil.toJsonStr(item));
                continue;
            }
            // 进程CPU占用率较大前5
            List<CollectProcessBean> processTop5List = item.getProcessTop5List();
            if (top5 && Objects.nonNull(processTop5List)) {
                for (CollectProcessBean ben : processTop5List) {
                    String collectTime = ben.getCollectTime();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String format = formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(collectTime)), ZoneId.systemDefault()));
                    ben.setCollectTime(format);
                }
                redisService.set(RedisCacheConst.TOP5_PROCESS_CPU + item.getAssetId(), JSONUtil.toJsonStr(processTop5List));
                top5 = false;
            }

            Date date = new Date();
            date.setTime(Long.parseLong(item.getCollectTime()));
            double cpuUsedRate = Double.parseDouble(item.getCpuUsedRate());
            if (cpuUsedRate > 100) {
                cpuUsedRate = 99.9;
                item.setCpuUsedRate(cpuUsedRate + "");
            }
            CollectCpu cpu = EntityBeanUtil.copy(item, CollectCpu.class);
            cpu.setId(MyIdUtil.getId());
            cpu.setCollectCode(collectCode);
            cpu.setCpuUsedRate(cpuUsedRate);
            cpu.setCollectTime(date);
            cpuList.add(cpu);

            addEvent(item);
        }
        if (cpuList.isEmpty()) {
            return;
        }
        // 实时数据更新
        cpuService.updateRealTimeData(cpuList);
        // 采集记录保存
        cpuService.saveBatch(cpuList);
    }

    /**
     * 触发事件
     *
     * @param item
     */
    private void addEvent(CollectCpuBean item) {
        String assetId = item.getAssetId();

        String key = "ADD_EVENT_CPU_" + assetId;
        synchronized (key.intern()) {
            VerifyThresholdReq req = new VerifyThresholdReq();
            req.setCollectValue(item.getCpuUsedRate());
            req.setAssetId(assetId);
            req.setHaveSection(true);
            req.setType(ThresholdSectionEnum.CPU);
            VerifyThresholdResp verifyResp = thresholdServ.verifyThreshold(req);

            Date date = new Date();
            date.setTime(Long.parseLong(item.getCollectTime()));

            List<CreateEventReq> needAddEvent = new ArrayList<CreateEventReq>();

            if (Objects.nonNull(verifyResp)) {
                CreateEventReq eventReq = new CreateEventReq();
                eventReq.setOriginalMsg(verifyResp.getMsg());
                eventReq.setEventLevel(verifyResp.getAlarmStatus() ? EventLevelEnum.ABNORMAL.getCode() : EventLevelEnum.NORMAL.getCode());
                eventReq.setAssetId(assetId);
                eventReq.setBaseValue(verifyResp.getBaseValue());
                eventReq.setCollectValue(item.getCpuUsedRate());
                eventReq.setUniqueCode(EventUniqueCode.CPU_UNIQUE_CODE);
                eventReq.setFlag("CPU阈值");
                eventReq.setGroupFlag(EventGroupConstant.CPU);
                eventReq.setCreateTime(date);
                needAddEvent.add(eventReq);
            }

            //区间阈值
            VerifyThresholdSectionResp thresholdSectionResp = thresholdServ.verifySectionThreshold(Double.valueOf(item.getCpuUsedRate()), EventUniqueCode.CPU_UNIQUE_CODE);
            List<CreateEventReq> cpuEventList = thresholdServ.disposeVerifyThresholdSectionResp(thresholdSectionResp, EventGroupConstant.CPU, date, assetId, item.getCpuUsedRate(), "CPU阈值");

            needAddEvent.addAll(cpuEventList);

            try {
                for (CreateEventReq createEventReq : needAddEvent) {
                    eventLogicServ.addEvent(createEventReq);
                }
            } catch (Exception e) {
                log.error("处理CPU事件异常：{}", e.getMessage(), e);
            }
        }

    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.CPU;
    }

}
