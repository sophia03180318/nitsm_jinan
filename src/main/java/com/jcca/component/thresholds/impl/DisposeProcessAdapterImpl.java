package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcca.admin.system.service.SysModuleConfigService;
import com.jcca.common.enums.StatusEnum;
import com.jcca.common.redis.service.RedisService;
import com.jcca.common.utils.*;
import com.jcca.common.utils.constants.AppLogHead;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.CreateEventReq;
import com.jcca.component.event.constant.EventGroupConstant;
import com.jcca.component.event.constant.EventUniqueCode;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectProcessBean;
import com.jcca.web.asset.controller.bean.AlarmVerifyBean;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.entity.ThresholdProcess;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.asset.service.ThresholdProcessService;
import com.jcca.web.collect.entity.CollectProcess;
import com.jcca.web.collect.service.CollectCpuService;
import com.jcca.web.collect.service.CollectProcessService;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 进程分析处理
 *
 * @author Lvyp
 */
@Slf4j
@Component
public class DisposeProcessAdapterImpl implements CollectAdapter {

    @Resource
    private CollectProcessService processService;
    @Resource
    private ThresholdProcessService thresholdService;
    @Resource
    private AssetService assetService;
    @Resource
    private EventLogicService eventLogicServ;
    @Resource
    private SysModuleConfigService sysModuleServ;
    @Resource
    private RedisService redisServ;

    private static Map<String, Integer> alarmCounterMap = new HashMap<String, Integer>();

    /**
     * 处理进程信息
     *
     * @param data
     */

    @Override
    public void dispose(JSONArray data) {
        List<CollectProcessBean> processBeans = JSONUtil.toList(data, CollectProcessBean.class);

        log.debug("--进程处理--：" + data.toString());

        // 车站设备需要看状态是否丢失
        processStatusDispose(processBeans);

        String collectCode = MyIdUtil.getId();
        List<CollectProcess> processList = new ArrayList<CollectProcess>();

        for (CollectProcessBean item : processBeans) {

            log.debug("处理进程阈值：" + JSONUtil.toJsonStr(item));

            String cpuRate = item.getCpuRate();
            String memoryRate = item.getMemoryRate();
            if (NumberUtil.isNumber(cpuRate) || NumberUtil.isDouble(cpuRate)) {
                int intValue = new BigDecimal(cpuRate).intValue();
                if (intValue < 0) {
                    item.setCpuRate("0");
                }
                if (intValue > 100 || intValue == 100) {
                    item.setCpuRate("100");
                }
            }
            if (NumberUtil.isNumber(memoryRate) || NumberUtil.isDouble(memoryRate)) {
                if (new BigDecimal(memoryRate).intValue() < 0) {
                    item.setMemoryRate("0");
                }
                if (new BigDecimal(memoryRate).intValue() > 100) {
                    item.setMemoryRate("100");
                }
            }

            if (StrUtil.isEmpty(item.getAssetId()) || StrUtil.isEmpty(item.getName()) || StrUtil.isEmpty(item.getProcessId())
                    || StrUtil.isEmpty(item.getCpuRate()) || StrUtil.isEmpty(item.getMemoryRate())) {
                continue;
            }

            if (!AppPattenUtils.isDouble(item.getCpuRate()) || !AppPattenUtils.isDouble(item.getMemoryRate())) {
                continue;
            }

            QueryWrapper<ThresholdProcess> queryWrapper = new QueryWrapper<ThresholdProcess>();
            queryWrapper.eq("ASSET_ID", item.getAssetId());
            queryWrapper.eq("PROCESS_NAME", item.getName());
            List<ThresholdProcess> thresholdList = thresholdService.list(queryWrapper);

            if (thresholdList.isEmpty()) {
                continue;
            }

            for (ThresholdProcess threshold : thresholdList) {
                threshold.setProcessId(item.getProcessId());
                thresholdService.updateById(threshold);
            }

            Date collectTime = new Date();
            collectTime.setTime(Long.parseLong(item.getCollectTime()));

            CollectProcess process = EntityBeanUtil.copy(item, CollectProcess.class);
            process.setCollectCode(collectCode);
            process.setCollectTime(collectTime);
            process.setId(MyIdUtil.getId());
            process.setMemoryRate(Double.valueOf(item.getMemoryRate()));
            process.setCpuRate(Double.valueOf(item.getCpuRate()));
            // 处理阈值告警
            disposeThreshold(process);

            processList.add(process);
        }

        if (processList.isEmpty()) {
            return;
        }

        // 保存
        processService.saveBatch(processList);
        // 更新实时数据
        processService.updateRealTimeData(processList);
    }

    /**
     * 处理进程丢失事件
     *
     * @param processBeans
     */
    private void processStatusDispose(List<CollectProcessBean> processBeans) {
        if (Objects.isNull(processBeans) || processBeans.isEmpty()) {
            return;
        }
        CollectProcessBean collectProcessBean = processBeans.get(0);
        String assetId = collectProcessBean.getAssetId();
        Boolean isStation = assetService.isStationAsset(assetId);
        if (isStation) {
            synchronized (assetId.intern()) {
                QueryWrapper<ThresholdProcess> queryWrapper = new QueryWrapper<ThresholdProcess>();
                queryWrapper.eq("ASSET_ID", assetId);
                List<ThresholdProcess> thresholdList = thresholdService.list(queryWrapper);

                List<String> collectProcessNameList = processBeans.stream().map(CollectProcessBean::getName)
                        .collect(Collectors.toList());

                AlarmVerifyBean alarmVerifyConf = sysModuleServ.getAlarmVerifyValue();

                for (ThresholdProcess thresholdProcess : thresholdList) {
                    String configProcessName = thresholdProcess.getProcessName();

                    Date collectTime = new Date();
                    collectTime.setTime(Long.valueOf(collectProcessBean.getCollectTime()));

                    CreateEventReq eventReq = new CreateEventReq();
                    eventReq.setAssetId(assetId);
                    eventReq.setUniqueCode(EventUniqueCode.PROCESS_STOP);
                    eventReq.setCreateTime(collectTime);
                    eventReq.setFlag(configProcessName);
                    eventReq.setGroupFlag(EventGroupConstant.PROCESS);

                    String cacheKey = String.format("%s_%s", assetId, configProcessName);

                    if (collectProcessNameList.toString().contains(configProcessName)) {
                        // 采集到了
                        List<CollectProcessBean> newProcess = processBeans.stream().filter(item -> item.getName().contains(configProcessName)).collect(Collectors.toList());
                        thresholdProcess.setProcessId(newProcess.get(0).getProcessId());

                        alarmCounterMap.remove(cacheKey);
                        String msg = String.format("进程【%s】，进程号【%s】状态正常！", configProcessName,
                                thresholdProcess.getProcessId());

                        eventReq.setOriginalMsg(msg);
                        eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());

                        thresholdProcess.setCollectStatus(StatusEnum.OK.getCode());

                    } else {
                        // 未采集到
                        Integer alarmCount = alarmCounterMap.get(cacheKey);
                        if (Objects.isNull(alarmCount)) {
                            alarmCount = 0;
                        }

                        String msg = String.format("进程【%s】，进程号【%s】丢失！", configProcessName,
                                thresholdProcess.getProcessId());

                        log.info("设定告警次数：{}", alarmVerifyConf.getProcessSize());
                        if (alarmCount >= alarmVerifyConf.getProcessSize()) {
                            //次数大于等于设定 上告警
                            eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                            thresholdProcess.setCollectStatus(StatusEnum.NO.getCode());
                        } else {
                            Asset asset = assetService.getById(assetId);
                            //清空状态再来一次
                            redisServ.delProcess(assetId,asset.getAssetCode());
                            log.info("累计告警次数：{}", alarmCount);

                            eventReq.setEventLevel(EventLevelEnum.WARNING.getCode());
                            msg = String.format("进程【%s】，进程号【%s】第%s次检查到丢失！%s", configProcessName,
                                    thresholdProcess.getProcessId(), alarmCount + 1, "由于设定了检查周期此次事件仅记录，不上告警！");
                            alarmCounterMap.put(cacheKey, alarmCount + 1);
                        }

                        eventReq.setOriginalMsg(msg);

                    }

                    try {
                        thresholdService.updateById(thresholdProcess);

                        log.debug("--进程处理--车站数据加事件：" + JSONUtil.toJsonStr(eventReq));
                        eventLogicServ.addEvent(eventReq);
                    } catch (Exception e) {
                        log.info(e.getMessage(), e);
                    }
                }

            }
        }
    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.PROCESS;
    }

    /**
     * 判断是否触发阈值告警
     *
     * @param item
     */
    private void disposeThreshold(CollectProcess item) {
        String key = item.getAssetId() + item.getName();

        synchronized (key.intern()) {
            QueryWrapper<ThresholdProcess> queryWrapper = new QueryWrapper<ThresholdProcess>();
            queryWrapper.eq("PROCESS_NAME", item.getName());
            queryWrapper.eq("ASSET_ID", item.getAssetId());
            ThresholdProcess threshold = thresholdService.getOne(queryWrapper);
            if (Objects.isNull(threshold)) {
                log.info(AppLogUtils.logStr(AppLogHead.DISPOSE_PROCESS_SERVICE, "该进程未配置阈值跳过阈值比较",
                        "assetid:" + item.getAssetId()));
                return;
            }

            threshold.setCpuRate(item.getCpuRate().toString());
            threshold.setMemoryRate(item.getMemoryRate().toString());
            threshold.setProcessId(item.getProcessId());
            thresholdService.updateById(threshold);

            // 添加事件
            String thresholdCpu = threshold.getThresholdCpu();
            String thresholdMemory = threshold.getThresholdMemory();
            if (StrUtil.isEmpty(thresholdCpu)) {
                thresholdCpu = "100";
            }
            if (StrUtil.isEmpty(thresholdMemory)) {
                thresholdMemory = "100";
            }

            Boolean cpuCompare = AppMathUtil.compare(item.getCpuRate() + "", thresholdCpu);
            Boolean memoryCompare = AppMathUtil.compare(item.getMemoryRate() + "", thresholdMemory);

            CreateEventReq eventReq = new CreateEventReq();

            eventReq.setCreateTime(item.getCollectTime());
            eventReq.setAssetId(item.getAssetId());
            eventReq.setBaseValue(thresholdCpu);
            eventReq.setCollectValue(item.getCpuRate() + "");
            eventReq.setFlag(item.getName());
            eventReq.setGroupFlag(EventGroupConstant.PROCESS_TH);

            eventReq.setUniqueCode(EventUniqueCode.TH_PROCESS_CPU);

            if (cpuCompare && StrUtil.isNotEmpty(thresholdCpu)) {
                eventReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                String format = String.format("进程【%s】采集到CPU使用率：%s %%,设定值：%s %%,超阈值！", item.getName(), item.getCpuRate(),
                        thresholdCpu);
                eventReq.setOriginalMsg(format);
            } else {
                eventReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                String format = String.format("进程【%s】采集到CPU使用率：%s %%,设定值：%s %%,指标正常！", item.getName(),
                        item.getCpuRate(), thresholdCpu);
                eventReq.setOriginalMsg(format);
            }

            CreateEventReq memoryReq = EntityBeanUtil.copy(eventReq, CreateEventReq.class);
            memoryReq.setBaseValue(thresholdMemory);
            memoryReq.setCollectValue(item.getMemoryRate() + "");

            memoryReq.setUniqueCode(EventUniqueCode.TH_PROCESS_MEMORY);
            if (memoryCompare && StrUtil.isNotEmpty(thresholdMemory)) {
                memoryReq.setEventLevel(EventLevelEnum.ABNORMAL.getCode());
                String format = String.format("进程【%s】采集到内存使用率：%s %%,设定值：%s %%,超阈值！", item.getName(),
                        item.getMemoryRate(), thresholdMemory);
                memoryReq.setOriginalMsg(format);
            } else {
                memoryReq.setEventLevel(EventLevelEnum.NORMAL.getCode());
                String format = String.format("进程【%s】采集到内存使用率：%s %%,设定值：%s %%,指标正常！", item.getName(),
                        item.getMemoryRate(), thresholdMemory);
                memoryReq.setOriginalMsg(format);
            }

            try {
                eventLogicServ.addEvent(eventReq);
                eventLogicServ.addEvent(memoryReq);
            } catch (Exception e) {
                log.error("处理内存事件异常：{}", e.getMessage(), e);
            }
        }

    }
}
