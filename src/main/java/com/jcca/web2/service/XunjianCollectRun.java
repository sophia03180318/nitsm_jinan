package com.jcca.web2.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.common.webssh.websocket.XunjianWebSocketHandler;
import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CommonEntity;
import com.jcca.dataProcessing.support.XunjianEvent;
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
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.inspectAssetService = SpringContextUtil.getBean(InspectAssetService.class);
        this.inspectDetailService = SpringContextUtil.getBean(InspectDetailService.class);
        this.inspectRecordService = SpringContextUtil.getBean(InspectRecordService.class);
        this.xunjianScheduleService = SpringContextUtil.getBean(XunjianScheduleService.class);
        while (true) {
            XunjianEvent event = Web2Const.XUNJIAN_COLLECT_QUEUE.take();
            CommonEntity info = event.getInfo();
            String inspectRecordId;
            XunjianDataDto dto;
            if (info == null) {
                dto = event.getDto();
                inspectRecordId = dto.getInspectRecordId();
            } else {
                inspectRecordId = info.getInspectRecordId();
                if (inspectRecordId == null) {
                    continue;
                }
                Map<String, ChangeInfo> maps = info.getMaps();
                System.out.println(JSONUtil.toJsonStr(info));
            }
            InspectRecord record;

            if (!inspectRecordMap.containsKey(inspectRecordId)) {
                record = inspectRecordService.getById(inspectRecordId);
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

            try {
//                this.send2Web(dto);
            } catch (Exception e) {
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检向前端发送数据异常", info);
            }

            TimeUnit.MILLISECONDS.sleep(500L);
        }
    }


    // <inspectRecordId, 巡检设备总数量>
    private final Map<String, Integer> assetTotalMap = new ConcurrentHashMap<>();

    // 设备指标总数量 <inspectRecordId, <assetId, 设备指标数量>>
    private final Map<String, Map<String, Long>> assetTargetCountMap = new ConcurrentHashMap<>();
    // 已巡检设备指标 <inspectRecordId, <assetId, 已巡检设备指标数量>>
    private final Map<String, Map<String, Integer>> currentAssetTargetMap = new ConcurrentHashMap<>();

    // <inspectRecordId, 巡检指标总数量>
    private final Map<String, Integer> targetTotalMap = new ConcurrentHashMap<>();
    // 已巡检指标数量 <inspectRecordId, 已巡检指标数量>
    private final Map<String, Integer> currentTargetCountMap = new ConcurrentHashMap<>();
    // 已巡检异常指标数量 <inspectRecordId, 已巡检异常指标数量>
    private final Map<String, Integer> targetAbnormalMap = new ConcurrentHashMap<>();
    // 已巡检正常指标数量 <inspectRecordId, 已巡检正常指标数量>
    private final Map<String, Integer> targetNormalMap = new ConcurrentHashMap<>();

    // 指标分类总数量 <inspectRecordId, <targetItem, 该指标总数量>>
    private final Map<String, Map<String, Long>> totalTargetMap = new ConcurrentHashMap<>();
    // 该指标已巡检数量 <inspectRecordId, <targetItem, 该指标已巡检数量>>
    private final Map<String, Map<String, Integer>> currentCountTargetMap = new ConcurrentHashMap<>();
    // 该指标异常数量 <inspectRecordId, <targetItem, 该指标异常数量>>
    private final Map<String, Map<String, Integer>> currentAbnormalTargetMap = new ConcurrentHashMap<>();
    // 该指标正常数量 <inspectRecordId, <targetItem, 该指标正常数量>>
    private final Map<String, Map<String, Integer>> currentNormalTargetMap = new ConcurrentHashMap<>();
    // 资产状态 <inspectRecordId, <assetId, 资产状态>>
    private final Map<String, Map<String, Integer>> assetStateMap = new ConcurrentHashMap<>();
    // 指标状态 <inspectRecordId, <assetId, 指标状态>>
    private final Map<String, Map<String, Integer>> targetStateMap = new ConcurrentHashMap<>();

    private int abnormal = 0, normal = 0;

    private void send2Web(XunjianDataDto dto) {
        String inspectRecordId = dto.getInspectRecordId();
        InspectRecord inspectRecord = inspectRecordMap.get(inspectRecordId);
        XunjianSchedule schedule = xunjianScheduleMap.get(inspectRecord.getScheduleId());
        String operator = schedule.getOperator();
        String jobId = schedule.getJobId();
        String assetId = dto.getAssetId();
        String assetName = dto.getAssetName();
        String targetItem = dto.getTargetItem();
        String targetState = dto.getInspectState();

        // 巡检设备及指标数量
        if (!assetTotalMap.containsKey(inspectRecordId)) {
            List<InspectAsset> assetList = inspectAssetService.getAllByJobId(jobId);
            inspectAssetMap.put(inspectRecordId, assetList);

            targetNameMap.put(inspectRecordId, new HashMap<>());
            for (InspectAsset asset : assetList) {
                targetNameMap.get(inspectRecordId).putIfAbsent(asset.getTargetItem(), asset.getTargetName());
            }
            Set<String> set = targetNameMap.get(inspectRecordId).keySet();
            if (!set.contains(targetItem)) {
                return;
            }

            Map<String, List<InspectAsset>> assetCollect = assetList.stream().collect(Collectors.groupingBy(InspectAsset::getAssetId));
            assetTotalMap.put(inspectRecordId, assetCollect.size());
            targetTotalMap.put(inspectRecordId, assetList.size());

            Map<String, Long> assetTargetCollect = assetList.stream().collect(Collectors.groupingBy(InspectAsset::getAssetId, Collectors.counting()));
            assetTargetCountMap.put(inspectRecordId, assetTargetCollect);

            Map<String, Long> collect = assetList.stream().collect(Collectors.groupingBy(InspectAsset::getTargetItem, Collectors.counting()));
            totalTargetMap.put(inspectRecordId, collect);
        }

        // 资产状态
        assetStateMap.computeIfAbsent(inspectRecordId, k -> new HashMap<>());
        Map<String, Integer> astateMap = assetStateMap.get(inspectRecordId);
        astateMap.put(assetId, Integer.parseInt(Web2Const.INSPECTED));
        Set<String> keySet1 = astateMap.keySet();
        for (String key : keySet1) {
            if (assetId.equals(key) && Integer.parseInt(targetState) > astateMap.get(assetId)) {
                astateMap.put(assetId, Integer.parseInt(Web2Const.INSPECT_ERROR));
                assetStateMap.put(inspectRecordId, astateMap);
            }
        }
        this.sendMsg(operator, XunjianWSDto.XUNJIANING_ASSET, jobId, assetId, assetName, astateMap.get(assetId)); // 资产状态

        // 已巡检指标数量
        targetStateMap.computeIfAbsent(inspectRecordId, k -> new HashMap<>());
        Map<String, Integer> tstateMap = targetStateMap.get(inspectRecordId);
        if (tstateMap.get(targetItem) == null) {
            tstateMap.put(targetItem, Integer.parseInt(targetState));
            targetStateMap.put(inspectRecordId, tstateMap);
        } else {
            if (Integer.parseInt(targetState) > tstateMap.get(targetItem)) {
                tstateMap.put(targetItem, Integer.parseInt(targetState));
                targetStateMap.put(inspectRecordId, tstateMap);
            }
        }

        if (Integer.parseInt(targetState) > tstateMap.get(targetItem)) {
            tstateMap.put(targetItem, Integer.parseInt(Web2Const.INSPECT_ERROR));
        }
        currentTargetCountMap.merge(inspectRecordId, 1, Integer::sum);
        if (Web2Const.INSPECT_ERROR.equals(targetState)) {
            abnormal++;
            targetAbnormalMap.put(inspectRecordId, abnormal);
            if (currentAbnormalTargetMap.get(inspectRecordId) == null) {
                Map<String, Integer> hashMap = new HashMap<>();
                hashMap.put(targetItem, 1);
                currentAbnormalTargetMap.put(inspectRecordId, hashMap);
            } else {
                Map<String, Integer> map = currentAbnormalTargetMap.get(inspectRecordId);
                Integer i = map.get(targetItem);
                if (i == null) {
                    i = 1;
                } else {
                    i++;
                }
                map.put(targetItem, i);
                currentAbnormalTargetMap.put(inspectRecordId, map);
            }
            Integer ab = 0, a = 0;
            if (currentAbnormalTargetMap.get(inspectRecordId) != null && currentAbnormalTargetMap.get(inspectRecordId).get(targetItem) != null) {
                ab = currentAbnormalTargetMap.get(inspectRecordId).get(targetItem);
            }
            if (currentNormalTargetMap.get(inspectRecordId) != null && currentNormalTargetMap.get(inspectRecordId).get(targetItem) != null) {
                a = currentNormalTargetMap.get(inspectRecordId).get(targetItem);
            }

            this.sendTargetMsg(operator, XunjianWSDto.TARGET_STATUS, jobId, targetItem, a, ab); // 某类指标状态
        } else {
            normal++;
            targetNormalMap.put(inspectRecordId, normal);
            Map<String, Integer> map = currentNormalTargetMap.get(inspectRecordId);
            if (map == null) {
                map = new HashMap<>();
                map.put(targetItem, 1);
                currentNormalTargetMap.put(inspectRecordId, map);
            }
            Integer i = map.get(targetItem);
            if (i == null) {
                i = 1;
            } else {
                i++;
            }
            map.put(targetItem, i);
            currentNormalTargetMap.put(inspectRecordId, map);
        }

        // 某类指标巡检完成
        Map<String, Integer> targetMap = currentCountTargetMap.get(inspectRecordId);
        if (targetMap == null) {
            targetMap = new ConcurrentHashMap<>();
            targetMap.put(targetItem, 1);
            currentCountTargetMap.put(inspectRecordId, targetMap);
        } else {
            Integer i = targetMap.get(targetItem);
            if (i == null) {
                i = 1;
            } else {
                i += 1;
            }
            targetMap.put(targetItem, i);
            currentCountTargetMap.put(inspectRecordId, targetMap);
        }

        if (totalTargetMap.get(inspectRecordId).get(targetItem).intValue() == currentCountTargetMap.get(inspectRecordId).get(targetItem)) {
            Integer ab = 0, a = 0;
            if (currentAbnormalTargetMap.get(inspectRecordId) != null && currentAbnormalTargetMap.get(inspectRecordId).get(targetItem) != null) {
                ab = currentAbnormalTargetMap.get(inspectRecordId).get(targetItem);
            }
            if (currentNormalTargetMap.get(inspectRecordId) != null && currentNormalTargetMap.get(inspectRecordId).get(targetItem) != null) {
                a = currentNormalTargetMap.get(inspectRecordId).get(targetItem);
            }

            this.sendTargetMsg(operator, XunjianWSDto.TARGET_STATUS, jobId, targetItem, a, ab); // 某类指标巡检完成
        }

        // 设备指标数量和已巡检设备指标数量相同则该设备巡检结束
        this.sendMsg(operator, XunjianWSDto.XUNJIANING_ASSET, jobId, assetId, assetName, Integer.parseInt(targetState)); // 当前巡检设备
        currentAssetTargetMap.computeIfAbsent(inspectRecordId, k -> new HashMap<>());
        Integer currentSize = currentAssetTargetMap.get(inspectRecordId).get(assetId);
        if (currentSize == null) {
            currentSize = 1;
        } else {
            currentSize += 1;
        }
        currentAssetTargetMap.get(inspectRecordId).put(assetId, currentSize);
        if (assetTargetCountMap.get(inspectRecordId).get(assetId) == currentSize.intValue()) {
            this.sendMsg(operator, XunjianWSDto.ASSET_STATUS, jobId, assetId, assetName, astateMap.get(assetId)); // 资产巡检完成
        }

        int i1 = targetNormalMap.get(inspectRecordId) == null ? 0 : targetNormalMap.get(inspectRecordId);
        int i2 = targetAbnormalMap.get(inspectRecordId) == null ? 0 : targetAbnormalMap.get(inspectRecordId);
        this.sendMsg(operator, XunjianWSDto.TARGET_COUNT, jobId, i1, i2); // 指标统计

        // 巡检总进度
        Integer totalTarget = targetTotalMap.get(inspectRecordId);
        Integer countTarget = currentTargetCountMap.get(inspectRecordId);
        BigDecimal process = new BigDecimal(countTarget).divide(new BigDecimal(totalTarget), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        this.sendMsg(operator, XunjianWSDto.WHOLE_PROCESS, jobId, "100", "进度条", process.intValue());

        // 已巡检指标数量和指标总数量相同则全部巡检结束
        if (targetTotalMap.get(inspectRecordId).intValue() == currentTargetCountMap.get(inspectRecordId)) {
            // 设置资产指标为初始状态
            List<InspectAsset> assetList = inspectAssetMap.get(inspectRecordId);
            for (InspectAsset asset : assetList) {
                asset.setInspectState(Web2Const.INSPECT);
            }
            inspectAssetService.updateBatchById(assetList);

            // 设置为结束巡检
            schedule.setJobState(Integer.parseInt(Web2Const.INSPECT));
            xunjianScheduleService.updateById(schedule);

            // 清空内存
            this.clearMap(inspectRecordId);

            normal = 0;
            abnormal = 0;
        }

        // 设置资产指标为巡检完成状态
        QueryWrapper<InspectAsset> query = Wrappers.query();
        query.eq("JOB_ID", jobId);
        query.eq("ASSET_ID", assetId);
        query.eq("TARGET_ITEM", targetItem);
        List<InspectAsset> list = inspectAssetService.list(query);
        for (InspectAsset asset : list) {
            asset.setInspectState(targetState);
            asset.setInspectValue(dto.getInspectValue());
            asset.setResultMsg(dto.getResultMsg());
            inspectAssetService.updateById(asset);

            // 保存巡检详情
            InspectDetail inspectDetail = new InspectDetail();
            BeanUtils.copyProperties(asset, inspectDetail);
            inspectDetail.setId(MyIdUtil.getId());
            inspectDetail.setInspectCode(inspectRecordId);
            inspectDetail.setInspectTime(new Date());
            inspectDetail.setResultMsg(dto.getResultMsg());
            inspectDetailService.save(inspectDetail);
        }
    }

    private void sendTargetMsg(String operator, Integer msgType, String jobId, String targetItem, int normal, int abnormal) {
        XunjianWSDto wsDto = new XunjianWSDto();
        wsDto.setUsername(operator);
        wsDto.setMsgType(msgType);
        XunjianWSDto msg = new XunjianWSDto();
        msg.setJobId(jobId);
        msg.setId(targetItem);
        msg.setNormal(normal);
        msg.setAbnormal(abnormal);
        wsDto.setMessage(msg);
        this.send(wsDto);
    }

    private void clearMap(String inspectRecordId) {
        inspectAssetMap.clear();
        inspectRecordMap.clear();
        assetTotalMap.remove(inspectRecordId);
        targetTotalMap.remove(inspectRecordId);
        currentTargetCountMap.remove(inspectRecordId);
        targetAbnormalMap.remove(inspectRecordId);
        targetNameMap.remove(inspectRecordId);
        currentAssetTargetMap.remove(inspectRecordId);
        currentCountTargetMap.remove(inspectRecordId);
        totalTargetMap.remove(inspectRecordId);
        assetTargetCountMap.remove(inspectRecordId);
        targetNormalMap.remove(inspectRecordId);
        currentAbnormalTargetMap.remove(inspectRecordId);
        assetStateMap.remove(inspectRecordId);
        targetStateMap.remove(inspectRecordId);
        currentNormalTargetMap.remove(inspectRecordId);
    }

    private synchronized void send(XunjianWSDto wsDto) {
        String operator = wsDto.getUsername();
        WebSocketSession webSocketSession = XunjianWebSocketHandler.XUNJIAN_WEBSOCKET_MAP.get(operator);
        if (webSocketSession == null) {
            return;
        }
        try {
            webSocketSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(wsDto)));
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "给前端发送巡检消息", wsDto);
        } catch (IOException e) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集给前端发送消息异常", wsDto);
        }
    }

    private void sendMsg(String operator, Integer msgType, String jobId, int normal, int abnormal) {
        WebSocketSession webSocketSession = XunjianWebSocketHandler.XUNJIAN_WEBSOCKET_MAP.get(operator);
        if (webSocketSession == null) {
            return;
        }
        XunjianWSDto wsDto = new XunjianWSDto();
        wsDto.setUsername(operator);
        wsDto.setMsgType(msgType);
        XunjianWSDto msg = new XunjianWSDto();
        msg.setJobId(jobId);
        msg.setAbnormal(abnormal);
        msg.setNormal(normal);
        wsDto.setMessage(msg);
        this.send(wsDto);
    }

    private void sendMsg(String operator, Integer msgType, String jobId, String id, String name, Integer status, Integer count) {
        XunjianWSDto wsDto = new XunjianWSDto();
        wsDto.setUsername(operator);
        wsDto.setMsgType(msgType);
        XunjianWSDto msg = new XunjianWSDto();
        msg.setJobId(jobId);
        msg.setId(id);
        msg.setName(name);
        msg.setStatus(status);
        msg.setCount(count);
        wsDto.setMessage(msg);
        this.send(wsDto);
    }

    private void sendMsg(String operator, Integer msgType, String jobId, String id, String name, Integer status) {
        this.sendMsg(operator, msgType, jobId, id, name, status, 0);
    }
}
