package com.jcca.web2.service;

import cn.hutool.json.JSONUtil;
import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.common.webssh.websocket.XunjianWebSocketHandler;
import com.jcca.web2.constant.Web2Const;
import com.jcca.web2.dto.xunjian.XunjianDataDto;
import com.jcca.web2.dto.xunjian.XunjianWSDto;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.entity.InspectDetail;
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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.inspectAssetService = SpringContextUtil.getBean(InspectAssetService.class);
        this.inspectDetailService = SpringContextUtil.getBean(InspectDetailService.class);
        this.inspectRecordService = SpringContextUtil.getBean(InspectRecordService.class);
        this.xunjianScheduleService = SpringContextUtil.getBean(XunjianScheduleService.class);
        while (true) {
            XunjianDataDto dto = Web2Const.XUNJIAN_COLLECT_QUEUE.take();
            this.send2Web(dto);
        }
    }

    private void send2Web(XunjianDataDto dto) {
        // 巡检任务 xunjian_schedule id == inspect_record_schedule_id  job_id == inspect_record_inspect_code
        // 巡检资产 inspect_asset job_id == xunjian_schedule_job_id
        // 巡检记录 inspect_record scheduled_id == xunjian_schedule_id  inspect_code == xunjian_schedule_job_id
        // 巡检明细 inspect_detail inspect_code == inspect_record_id
        String inspectRecordId = dto.getInspectRecordId();
        String operator = dto.getOperator();

        // 巡检设备
        Map<String, Integer> assetStateMap = new HashMap<>();
        Map<String, Integer> targetStateMap = new HashMap<>();
        List<InspectDetail> detailList = new ArrayList<>();
        List<InspectAsset> assetList = inspectAssetService.getAllByJobId(dto.getJobId());
        Map<String, List<InspectAsset>> assetCollect = assetList.stream().collect(Collectors.groupingBy(InspectAsset::getAssetId));
        Map<String, Integer> assetTargetMap = new HashMap<>();
        assetCollect.keySet().forEach(key -> {
            assetTargetMap.put(key, assetCollect.get(key).size());
        });

        Map<String, Long> targetItemMap = assetList.stream().collect(Collectors.groupingBy(InspectAsset::getTargetItem, Collectors.counting()));

        Map<String, Integer> processMap = new HashMap<>();
        Map<String, Integer> targetMap = new HashMap<>();
        Map<String, Integer> targetAbnormalMap = new HashMap<>();
        int total = assetList.size();
        int count = 0, normal = 0, abnormal = 0;
        for (InspectAsset asset : assetList) {
            String assetId = asset.getAssetId();
            count++;
            // 设置资产指标为巡检中状态
            asset.setInspectState(Web2Const.INSPECTING);
            inspectAssetService.updateById(asset);

            this.sendMsg(operator, XunjianWSDto.XUNJIANING_ASSET, asset.getJobId(), asset.getAssetId(), asset.getAssetName(), assetStateMap.get(assetId));
            String result = Web2Const.INSPECT_ERROR;
            InspectDetail detail = null;
            try {
                detail = inspectAssetService.xunjianCollect(asset);
                result = detail.getInspectState();
            } catch (Exception e) {
                AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_MANAGE, "巡检采集异常", asset);
                detail = new InspectDetail();
                detail.setInspectState(Web2Const.INSPECT_ERROR);
                detail.setInspectValue("--");
                detail.setResultMsg("巡检采集异常");
            }

            // 指标实时统计
            if (result.equals(Web2Const.INSPECTED)) {
                normal++;
            }
            if (result.equals(Web2Const.INSPECT_ERROR)) {
                abnormal++;
            }
            this.sendMsg(operator, XunjianWSDto.TARGET_COUNT, asset.getJobId(), normal, abnormal);

            this.inspectProcess(operator, asset, assetStateMap, targetStateMap, assetTargetMap, processMap, targetMap,
                    targetAbnormalMap, targetItemMap, result);

            // 设置资产指标为巡检完成状态
            asset.setInspectState(detail.getInspectState());
            asset.setInspectValue(detail.getInspectValue());
            asset.setResultMsg(detail.getResultMsg());
            inspectAssetService.updateById(asset);

            BigDecimal process = new BigDecimal(count).divide(new BigDecimal(total), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
            this.sendMsg(operator, XunjianWSDto.WHOLE_PROCESS, asset.getJobId(), "100", "进度条", process.intValue());

            // 保存巡检详情
            InspectDetail inspectDetail = new InspectDetail();
            BeanUtils.copyProperties(asset, inspectDetail);
            inspectDetail.setId(MyIdUtil.getId());
            inspectDetail.setInspectCode(inspectRecordId);
            inspectDetail.setInspectTime(new Date());
            inspectDetail.setResultMsg(detail.getResultMsg());
            detailList.add(inspectDetail);

            if (detailList.size() >= 900) {
                inspectDetailService.saveBatch(detailList);
                detailList.clear();
            }
        }
        if (!detailList.isEmpty()) {
            inspectDetailService.saveBatch(detailList);
        }


        // 设置资产指标为初始状态
        for (InspectAsset asset : assetList) {
            asset.setInspectState(Web2Const.INSPECT);
        }
        inspectAssetService.updateBatchById(assetList, 900);

        // 推送完成消息
        this.sendMsg(operator, XunjianWSDto.WHOLE_PROCESS, schedule.getJobId(), "100", "进度条", 100);
    }


    private void inspectProcess(String operator, InspectAsset asset, Map<String, Integer> assetStateMap, Map<String, Integer> targetStateMap,
                                Map<String, Integer> assetTargetMap, Map<String, Integer> processMap, Map<String, Integer> targetMap,
                                Map<String, Integer> targetAbnormalMap, Map<String, Long> targetItemMap, String result) {
        String assetId = asset.getAssetId();
        String targetItem = asset.getTargetItem();
        int state = Integer.parseInt(result);

        // 资产进度
        if (assetStateMap.get(assetId) == null) {
            assetStateMap.put(assetId, state);
        } else {
            if (assetStateMap.get(assetId) < state) {
                assetStateMap.put(assetId, state);
            }
        }
        if (processMap.get(assetId) == null) {
            processMap.put(assetId, 1);
        } else {
            processMap.put(assetId, processMap.get(assetId) + 1);
            if (processMap.get(assetId).intValue() == assetTargetMap.get(assetId).intValue()) {
                this.sendMsg(operator, XunjianWSDto.ASSET_STATUS, asset.getJobId(), assetId, asset.getAssetName(), assetStateMap.get(assetId));
            }
        }

        // 指标进度
        if (targetStateMap.get(targetItem) == null) {
            targetStateMap.put(targetItem, state);
        } else {
            if (targetStateMap.get(targetItem) < state) {
                targetStateMap.put(targetItem, state);
            }
        }
        if (targetStateMap.get(targetItem) == 4) {
            targetAbnormalMap.merge(targetItem, 1, Integer::sum);
            this.sendMsg(operator, XunjianWSDto.TARGET_STATUS, asset.getJobId(), targetItem,
                    asset.getTargetName(), targetStateMap.get(targetItem), targetAbnormalMap.get(targetItem));
        }
        if (targetMap.get(targetItem) == null) {
            targetMap.put(targetItem, 1);
            if (targetItemMap.get(targetItem).intValue() == 1) {
                this.sendMsg(operator, XunjianWSDto.TARGET_STATUS, asset.getJobId(), targetItem,
                        asset.getTargetName(), targetStateMap.get(targetItem), targetAbnormalMap.get(targetItem));
            }
        } else {
            targetMap.put(targetItem, targetMap.get(targetItem) + 1);
            if (targetMap.get(targetItem) == targetItemMap.get(targetItem).intValue()) {
                this.sendMsg(operator, XunjianWSDto.TARGET_STATUS, asset.getJobId(), targetItem,
                        asset.getTargetName(), targetStateMap.get(targetItem), targetAbnormalMap.get(targetItem));
            }
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

    private synchronized void send(XunjianWSDto wsDto) {
        String operator = wsDto.getUsername();
        WebSocketSession webSocketSession = XunjianWebSocketHandler.XUNJIAN_WEBSOCKET_MAP.get(operator);
        if (webSocketSession == null) {
            return;
        }
        try {
            webSocketSession.sendMessage(new TextMessage(JSONUtil.toJsonStr(wsDto)));
            AppLogUtils.buildLogInfo(LogFunctionEnum.XUNJIAN_REALTIME, "给前端发送巡检消息", JSONUtil.toJsonStr(wsDto));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
