package com.jcca.web2.service;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dto.xunjian.XunjianDataDto;
import com.jcca.web2.dto.xunjian.XunjianWSDto;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.entity.XunjianSchedule;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author: hhw
 * @description: XunjianCollectRun 主要是用来处理巡检采集数据
 * @date: 2025-05-27  16:16
 * @since: 2.1.6.0
 */
@Component
public class XunjianCollectRun implements ApplicationRunner {

    private InspectAssetService inspectAssetService;
    private InspectDetailService inspectDetailService;
    private InspectRecordService inspectRecordService;
    private XunjianScheduleService xunjianScheduleService;

    private final Map<String, XunjianSchedule> xunjianScheduleMap = new HashMap<>();

    // <inspectRecordId, <targetItem, targetName>>
    private final Map<String, Map<String, String>> targetNameMap = new ConcurrentHashMap<>();
    private final Map<String, List<InspectAsset>> inspectAssetMap = new ConcurrentHashMap<>();
    private final Map<String, InspectRecord> inspectRecordMap = new ConcurrentHashMap<>();
    // 资产ID 名称对应，<assetId, assetName>
    private final Map<String, String> assetIdName = new HashMap<>(256);


    @Override
    public void run(ApplicationArguments args) throws Exception {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) SpringContextUtil.getBean(ThreadPoolEnum.xunjianExecutor);
        executor.execute(() -> {
            try {
                go();
            } catch (InterruptedException ignored) {

            }
        });
    }

    private void go() throws InterruptedException {
        this.inspectAssetService = SpringContextUtil.getBean(InspectAssetService.class);
        this.inspectDetailService = SpringContextUtil.getBean(InspectDetailService.class);
        this.inspectRecordService = SpringContextUtil.getBean(InspectRecordService.class);
        this.xunjianScheduleService = SpringContextUtil.getBean(XunjianScheduleService.class);
        while (true) {
            IEvent event = Web2Const.XUNJIAN_COLLECT_QUEUE.take();
            Integer xunjianIsFinish = event.getXunjianIsFinish();
            String inspectRecordId;
            XunjianDataDto dto;
            if (event.getXunjianDataDto() == null) {
                inspectRecordId = event.getInspectRecordId();
                if (inspectRecordId == null) {
                    continue;
                }
                dto = new XunjianDataDto();
                if (event.getEventAlarmLevelBaseEntity() != null) {
                    dto.setEventTypeId(event.getEventAlarmLevelBaseEntity().getEventTypeId());
                }
                dto.setInspectRecordId(inspectRecordId);
                dto.setAssetId(event.getAssetId());
                dto.setTargetItem(event.getEventRedisKey());
                if (event.getStatus() != null) {
                    dto.setInspectState(event.getStatus() == -1 ? Web2Const.INSPECT_ERROR : Web2Const.INSPECTED);
                }
                if (event.getInfo() != null) {
                    dto.setInspectValue(event.getInfo().getValue() + "");
                }
                dto.setResultMsg(event.getDescStr());
                dto.setAlarmId(event.getAlarmId());
            } else {
                dto = event.getXunjianDataDto();
                inspectRecordId = dto.getInspectRecordId();
            }
            dto.setXunjianIsFinish(xunjianIsFinish);

            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "巡检接收到数据", dto);

            InspectRecord record;

            if (inspectRecordMap.get(inspectRecordId) == null) {
                record = inspectRecordService.getById(inspectRecordId);
                if (record == null) {
                    continue;
                }
                inspectRecordMap.put(inspectRecordId, record);
            }
            record = inspectRecordMap.get(inspectRecordId);
            if (record == null) {
                continue;
            }

            XunjianSchedule schedule;
            String scheduleId = record.getScheduleId();
            if (!xunjianScheduleMap.containsKey(scheduleId)) {
                schedule = xunjianScheduleService.getById(scheduleId);
                xunjianScheduleMap.put(scheduleId, schedule);
            }
            schedule = xunjianScheduleMap.get(scheduleId);
            if (schedule == null) {
                continue;
            }

            this.send2Web(dto);

            TimeUnit.MILLISECONDS.sleep(100L);
        }
    }


    // <inspectRecordId, 巡检设备总数量>
    private final Map<String, Integer> assetTotalMap = new ConcurrentHashMap<>();

    // 设备指标总数量 <inspectRecordId, <assetId, 设备指标数量>>
    private final Map<String, Map<String, Long>> assetTargetCountMap = new ConcurrentHashMap<>();
    // 已巡检设备指标 <inspectRecordId, <assetId, 已巡检设备指标数量>>
    private final Map<String, Map<String, Integer>> currentAssetTargetMap = new ConcurrentHashMap<>();

    // <inspectRecordId, 巡检指标总数量>
    public static final Map<String, Integer> targetTotalMap = new ConcurrentHashMap<>();
    // 已巡检指标数量 <inspectRecordId, 已巡检指标数量>
    public static final Map<String, Integer> currentTargetCountMap = new ConcurrentHashMap<>();
    // 已巡检异常指标数量 <inspectRecordId, <已巡检异常指标数量>>
    public static final Map<String, Set<String>> targetAbnormalSet = new ConcurrentHashMap<>();
    // 已巡检正常指标数量 <inspectRecordId, <已巡检正常指标数量>>
    public static final Map<String, Set<String>> targetNormalSet = new ConcurrentHashMap<>();

    // 指标分类总数量 <inspectRecordId, <targetItem, 该指标总数量>>
    private final Map<String, Map<String, Long>> totalTargetMap = new ConcurrentHashMap<>();
    // 该指标已巡检数量 <inspectRecordId, <targetItem, 该指标已巡检数量>>
    private final Map<String, Map<String, Integer>> currentCountTargetMap = new ConcurrentHashMap<>();
    // 该指标异常数量 <inspectRecordId, <eventTypeId, <assetId>>>
    private final Map<String, Map<String, Set<String>>> currentAbnormalTargetMap = new ConcurrentHashMap<>();
    // 该指标正常数量 <inspectRecordId, <eventTypeId>>
    private final Map<String, Set<String>> currentNormalTargetMap = new ConcurrentHashMap<>();
    // 资产状态 <inspectRecordId, <assetId, 资产状态>>
    private final Map<String, Map<String, Integer>> assetStateMap = new ConcurrentHashMap<>();
    // 指标状态 <inspectRecordId, <eventTypeId, 指标状态>>
    private final Map<String, Map<String, Integer>> targetStateMap = new ConcurrentHashMap<>();
    // 重复指标 <inspectRecordId, <targetItem>>
    private final Map<String, Set<String>> repeatTargetMap = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> repeatEventTypeIdMap = new ConcurrentHashMap<>();
    // 指标大类型总数
    private final Map<String, Set<String>> totalMap = new ConcurrentHashMap<>();

    private synchronized void send2Web(XunjianDataDto dto) {

        String inspectRecordId = dto.getInspectRecordId();
        InspectRecord inspectRecord = inspectRecordMap.get(inspectRecordId);
        XunjianSchedule schedule = xunjianScheduleMap.get(inspectRecord.getScheduleId());
        String operator = schedule.getOperator();
        String jobId = schedule.getJobId();
        dto.setJobId(jobId);
        String assetId = dto.getAssetId();
        String targetItem = dto.getTargetItem();
        String targetState = dto.getInspectState();
        String eventTypeId = dto.getEventTypeId();
        Integer xunjianIsFinish = dto.getXunjianIsFinish();
        // 巡检设备及指标数量
        if (!assetTotalMap.containsKey(inspectRecordId)) {
            List<InspectAsset> assetList = inspectAssetService.getAllByJobId(jobId);
            targetNameMap.put(inspectRecordId, new HashMap<>());
            for (InspectAsset asset : assetList) {
                targetNameMap.get(inspectRecordId).put(asset.getTargetItem(), asset.getTargetName());
            }
            Set<String> set = targetNameMap.get(inspectRecordId).keySet();
            if (!set.contains(targetItem)) {
                return;
            }

            inspectAssetMap.put(inspectRecordId, assetList);

            Set<String> totalSet = new HashSet<>();
            for (InspectAsset inspectAsset : assetList) {
                assetIdName.put(inspectAsset.getAssetId(), inspectAsset.getAssetName());

                totalSet.add(inspectAsset.getEventTypeId());
                totalMap.put(inspectRecordId, totalSet);
            }

            Map<String, List<InspectAsset>> assetCollect = assetList.stream().collect(Collectors.groupingBy(InspectAsset::getAssetId));
            assetTotalMap.put(inspectRecordId, assetCollect.size());
            targetTotalMap.put(inspectRecordId, assetList.size());

            Map<String, Long> assetTargetCollect = assetList.stream().collect(Collectors.groupingBy(InspectAsset::getAssetId, Collectors.counting()));
            assetTargetCountMap.put(inspectRecordId, assetTargetCollect);

            Map<String, Long> collect = assetList.stream().collect(Collectors.groupingBy(InspectAsset::getEventTypeId, Collectors.counting()));
            totalTargetMap.put(inspectRecordId, collect);
        }

        Set<String> set = targetNameMap.get(inspectRecordId).keySet();
        if (targetItem != null && !set.contains(targetItem)) {
            return;
        }

        // 过滤重复指标
        Set<String> targets = repeatTargetMap.get(inspectRecordId);
        if (targets == null) {
            targets = new HashSet<>();
        }
        String idItem = assetId + targetItem;
        if (!targets.contains(idItem)) {
            currentTargetCountMap.merge(inspectRecordId, 1, Integer::sum);
            targets.add(idItem);
            repeatTargetMap.put(inspectRecordId, targets);
        }

        String assetName = assetIdName.get(assetId);

        if (xunjianIsFinish != null && xunjianIsFinish == 1) {
            // 设置资产指标为初始状态
            List<InspectAsset> assetList = inspectAssetMap.get(inspectRecordId);
            for (InspectAsset asset : assetList) {
                asset.setInspectState(Web2Const.INSPECT);
            }
            inspectAssetService.updateBatchById(assetList);

            // 设置为结束巡检
            schedule.setJobState(Integer.parseInt(Web2Const.INSPECT));
            schedule.setLastTime(new Date());
            xunjianScheduleService.updateById(schedule);

            this.sendMsg(operator, XunjianWSDto.WHOLE_PROCESS, jobId, "100", "进度条", 100);
            // 清空内存
            this.clearMap(inspectRecordId);
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "巡检结束", inspectRecordId);
            return;
        }

        // 资产状态
        if (assetStateMap.get(inspectRecordId) == null) {
            Map<String, Integer> hashMap = new HashMap<>();
            hashMap.put(assetId, Integer.parseInt(targetState));
            assetStateMap.put(inspectRecordId, hashMap);
        } else {
            Map<String, Integer> astateMap = assetStateMap.get(inspectRecordId);
            Integer i = astateMap.get(assetId);
            if (i == null) {
                astateMap.put(assetId, Integer.parseInt(targetState));
            } else {
                if (Integer.parseInt(targetState) > i) {
                    astateMap.put(assetId, Integer.parseInt(Web2Const.INSPECT_ERROR));
                }
            }
            assetStateMap.put(inspectRecordId, astateMap);
        }
        this.sendMsg(operator, XunjianWSDto.XUNJIANING_ASSET, jobId, assetId, assetName, assetStateMap.get(inspectRecordId).get(assetId)); // 当前巡检资产

        // 已巡检指标数量
        targetStateMap.computeIfAbsent(inspectRecordId, k -> new HashMap<>());
        Map<String, Integer> tstateMap = targetStateMap.get(inspectRecordId);
        if (tstateMap.get(eventTypeId) == null) {
            tstateMap.put(eventTypeId, Integer.parseInt(targetState));
            targetStateMap.put(inspectRecordId, tstateMap);
        } else {
            if (Integer.parseInt(targetState) > tstateMap.get(eventTypeId)) {
                tstateMap.put(eventTypeId, Integer.parseInt(targetState));
                targetStateMap.put(inspectRecordId, tstateMap);
            }
        }
        if (Integer.parseInt(targetState) > tstateMap.get(eventTypeId)) {
            tstateMap.put(eventTypeId, Integer.parseInt(Web2Const.INSPECT_ERROR));
        }

        // 保存巡检详情
        this.saveDetail(dto);

        if (Web2Const.INSPECT_ERROR.equals(targetState)) {
            if (targetAbnormalSet.get(inspectRecordId) == null) {
                Set<String> sset = new HashSet<>();
                sset.add(eventTypeId);
                targetAbnormalSet.put(inspectRecordId, sset);
            } else {
                targetAbnormalSet.get(inspectRecordId).add(eventTypeId);
            }

            if (currentAbnormalTargetMap.get(inspectRecordId) == null) {
                Map<String, Set<String>> map = new HashMap<>();
                Set<String> set1 = new HashSet<>();
                set1.add(assetId);
                map.put(eventTypeId, set1);
                currentAbnormalTargetMap.put(inspectRecordId, map);
            } else {
                Map<String, Set<String>> stringSetMap = currentAbnormalTargetMap.get(inspectRecordId);
                Set<String> set1 = stringSetMap.get(eventTypeId);
                if (set1 == null) {
                    set1 = new HashSet<>();
                }
                set1.add(assetId);
                stringSetMap.put(eventTypeId, set1);
                currentAbnormalTargetMap.put(inspectRecordId, stringSetMap);
            }

            Set<String> setEventType = repeatEventTypeIdMap.get(inspectRecordId);
            if (setEventType == null) {
                setEventType = new HashSet<>();
            }
            if (!setEventType.contains(eventTypeId)) {
                this.sendTargetMsg(operator, XunjianWSDto.TARGET_STATUS, jobId, eventTypeId); // 异常指标大类型
            }
            setEventType.add(eventTypeId);

        }
        // 某类指标巡检完成
        Map<String, Integer> targetMap = currentCountTargetMap.get(inspectRecordId);
        if (targetMap == null) {
            targetMap = new ConcurrentHashMap<>();
            targetMap.put(eventTypeId, 1);
            currentCountTargetMap.put(inspectRecordId, targetMap);
        } else {
            Integer i = targetMap.get(eventTypeId);
            if (i == null) {
                i = 1;
            } else {
                i += 1;
            }
            targetMap.put(eventTypeId, i);
            currentCountTargetMap.put(inspectRecordId, targetMap);
        }

        Integer i = currentCountTargetMap.get(inspectRecordId).get(eventTypeId);
        Long l = totalTargetMap.get(inspectRecordId).get(eventTypeId);
        if (l.intValue() == i) {
            int ab = 0;
            if (currentAbnormalTargetMap.get(inspectRecordId) != null && currentAbnormalTargetMap.get(inspectRecordId).get(eventTypeId) != null) {
                ab = currentAbnormalTargetMap.get(inspectRecordId).get(eventTypeId).size();
            }

            if (ab == 0) {
                if (currentNormalTargetMap.get(inspectRecordId) == null) {
                    Set<String> set1 = new HashSet<>();
                    set1.add(eventTypeId);
                    currentNormalTargetMap.put(inspectRecordId, set1);
                } else {
                    Set<String> set1 = currentNormalTargetMap.get(inspectRecordId);
                    if (set1 == null) {
                        set1 = new HashSet<>();
                    }
                    set1.add(eventTypeId);
                    currentNormalTargetMap.put(inspectRecordId, set1);
                }
            }

            if (targetAbnormalSet.get(inspectRecordId) != null) {
                Set<String> abset = targetAbnormalSet.get(inspectRecordId);
                if (!abset.contains(eventTypeId)) {
                    if (targetNormalSet.get(inspectRecordId) == null) {
                        Set<String> sset = new HashSet<>();
                        sset.add(eventTypeId);
                        targetNormalSet.put(inspectRecordId, sset);
                    } else {
                        targetNormalSet.get(inspectRecordId).add(eventTypeId);
                    }
                }
            }
        }

        // 设备指标数量和已巡检设备指标数量相同则该设备巡检结束
        currentAssetTargetMap.computeIfAbsent(inspectRecordId, k -> new HashMap<>());
        Integer currentSize = currentAssetTargetMap.get(inspectRecordId).get(assetId);
        if (currentSize == null) {
            currentSize = 1;
        } else {
            currentSize += 1;
        }
        currentAssetTargetMap.get(inspectRecordId).put(assetId, currentSize);
        if (Web2Const.INSPECT_ERROR.equals(targetState)) {
            this.sendMsg(operator, XunjianWSDto.ASSET_STATUS, jobId, assetId, assetName, assetStateMap.get(inspectRecordId).get(assetId)); // 资产状态
        }

        // 巡检总进度
        Integer totalTarget = targetTotalMap.get(inspectRecordId);
        Integer countTarget = currentTargetCountMap.get(inspectRecordId);
        BigDecimal process = new BigDecimal(countTarget).divide(new BigDecimal(totalTarget), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        this.sendMsg(operator, XunjianWSDto.WHOLE_PROCESS, jobId, "100", "进度条", process.intValue());
    }

    private void saveDetail(XunjianDataDto dto) {
        List<InspectAsset> inspectAssets = inspectAssetMap.get(dto.getInspectRecordId());
        for (InspectAsset asset : inspectAssets) {
            if (asset.getAssetId().equals(dto.getAssetId()) && asset.getTargetItem().equals(dto.getTargetItem())) {
                asset.setInspectState(targetStateMap.get(dto.getInspectRecordId()).get(dto.getEventTypeId()) + "");
                asset.setInspectValue(dto.getInspectValue());
                asset.setResultMsg(dto.getResultMsg());
                inspectAssetService.updateById(asset);

                // 保存巡检详情
                InspectDetail inspectDetail = new InspectDetail();
                BeanUtils.copyProperties(asset, inspectDetail);
                inspectDetail.setId(MyIdUtil.getId());
                inspectDetail.setInspectCode(dto.getInspectRecordId());
                inspectDetail.setInspectState(dto.getInspectState());
                inspectDetail.setInspectTime(new Date());
                inspectDetail.setResultMsg(dto.getResultMsg());
                inspectDetail.setAlarmId(dto.getAlarmId());
                inspectDetail.setInspectValue(dto.getInspectValue());
                inspectDetail.setResultMsg(dto.getResultMsg());
                inspectDetailService.save(inspectDetail);
            }
        }
    }

    private void sendTargetMsg(String operator, Integer msgType, String jobId, String targetItem) {
        XunjianWSDto wsDto = new XunjianWSDto();
        wsDto.setUsername(operator);
        wsDto.setMsgType(msgType);
        XunjianWSDto msg = new XunjianWSDto();
        msg.setJobId(jobId);
        msg.setId(targetItem);
        msg.setNormal(0);
        msg.setAbnormal(1);
        wsDto.setMessage(msg);
        xunjianScheduleService.sendWsMsg(wsDto);
    }

    public synchronized void clearMap(String inspectRecordId) {
        assetTotalMap.remove(inspectRecordId);
        inspectRecordMap.remove(inspectRecordId);
        inspectAssetMap.remove(inspectRecordId);
        targetTotalMap.remove(inspectRecordId);
        currentTargetCountMap.remove(inspectRecordId);
        targetNameMap.remove(inspectRecordId);
        currentAssetTargetMap.remove(inspectRecordId);
        currentCountTargetMap.remove(inspectRecordId);
        totalTargetMap.remove(inspectRecordId);
        assetTargetCountMap.remove(inspectRecordId);
        currentAbnormalTargetMap.remove(inspectRecordId);
        assetStateMap.remove(inspectRecordId);
        targetStateMap.remove(inspectRecordId);
        currentNormalTargetMap.remove(inspectRecordId);
        repeatTargetMap.remove(inspectRecordId);
        repeatEventTypeIdMap.remove(inspectRecordId);
        totalMap.remove(inspectRecordId);
    }

    private void sendMsg(String operator, Integer msgType, String jobId, String id, String name, Integer status) {
        XunjianWSDto wsDto = new XunjianWSDto();
        wsDto.setUsername(operator);
        wsDto.setMsgType(msgType);
        XunjianWSDto msg = new XunjianWSDto();
        msg.setJobId(jobId);
        msg.setId(id);
        msg.setName(name);
        msg.setStatus(status);
        msg.setCount(0);
        wsDto.setMessage(msg);
        xunjianScheduleService.sendWsMsg(wsDto);
    }
}
