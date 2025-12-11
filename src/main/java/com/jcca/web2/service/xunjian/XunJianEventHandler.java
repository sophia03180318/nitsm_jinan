package com.jcca.web2.service.xunjian;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.web2.constant.XunJianConst;
import com.jcca.web2.dto.xunjian.InspectBaseDataWsVo;
import com.jcca.web2.dto.xunjian.InspectSession;
import com.jcca.web2.dto.xunjian.XunjianDataDto;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.enums.xunjian.InspectionStatus;
import com.jcca.web2.service.InspectAssetService;
import com.jcca.web2.service.InspectDetailService;
import com.jcca.web2.service.InspectRecordService;
import com.jcca.web2.service.XunjianScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author lifp
 * @version 1.0
 * @description: 智能巡检 - 核心处理器
 * @date 2025-12-04 星期四 13:58:15
 */
@Component
@RequiredArgsConstructor
public class XunJianEventHandler {

    private final InspectRecordService inspectRecordService;
    private final XunjianScheduleService xunjianScheduleService;
    private final InspectAssetService inspectAssetService;
    private final InspectDetailService inspectDetailService;
    private final XunjianNotifier notifier;
    private final InspectSessionManager sessionManager;

    public boolean handleEvent(IEvent event) {
        XunjianDataDto dto = buildDto(event);
        String inspectRecordId = dto.getInspectRecordId();
        String targetItem = dto.getTargetItem();
        if (inspectRecordId == null) {
            return false;
        }

        InspectRecord record = inspectRecordService.getById(inspectRecordId);
        if (record == null) {
            return false;
        }

        XunjianSchedule schedule = xunjianScheduleService.getById(record.getScheduleId());
        if (schedule == null) {
            return false;
        }

        InspectSession session = sessionManager.getSession(inspectRecordId);
        if (session == null) {
            AppLogUtils.buildLogWarn(LogFunctionEnum.XUNJIAN_REALTIME, "收到事件但会话已不存在，可能任务已结束", inspectRecordId);
            return false;
        }

        try {
            boolean isFinish = event.getStatus() != null && event.getStatus() == XunJianConst.FINISH_FLAG;
            // 单个设备采集结束
            if (isFinish) {
                String assetId = event.getAssetId();
                // 更新当前指标为已完成
                session.markAssetAsCompleted(assetId);
                // 移除当前指标
                XunJianConst.currentAssetIdMap.remove(inspectRecordId);
            }

            // 只处理任务中预定义的指标
            if (targetItem != null && !session.getAllowedTargetItems().containsKey(targetItem)) {
                return false;
            }
            processInspectionData(session, dto, schedule, isFinish);
            return isFinish;
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.XUNJIAN_REALTIME, "处理巡检事件异常", e);
            return false;
        }
    }

    /**
     * 构建 XunjianDataDto
     *
     * @param event
     * @return
     */
    private XunjianDataDto buildDto(IEvent event) {
        XunjianDataDto dto;
        if (event.getXunjianDataDto() == null) {
            dto = new XunjianDataDto();
            dto.setInspectRecordId(event.getInspectRecordId());
            dto.setAssetId(event.getAssetId());
            dto.setTargetItem(event.getEventRedisKey());
            if (event.getEventAlarmLevelBaseEntity() != null) {
                dto.setEventTypeId(event.getEventAlarmLevelBaseEntity().getEventTypeId());
            }
            if (event.getStatus() != null) {
                dto.setInspectState(event.getStatus() == -1 ? InspectionStatus.INSPECT_ALARM.getCode() : InspectionStatus.INSPECTED.getCode());
            }
            if (event.getInfo() != null) {
                dto.setInspectValue(event.getInfo().getValue() + "");
            }
            dto.setResultMsg(event.getXunjianDesc());
            dto.setAlarmId(event.getAlarmId());
        } else {
            dto = event.getXunjianDataDto();
        }
        dto.setXunjianIsFinish(event.getXunjianIsFinish());
        return dto;
    }

    /**
     *
     * @param session
     * @param dto
     * @param schedule
     * @param isAssetFinished
     */
    private void processInspectionData(InspectSession session, XunjianDataDto dto, XunjianSchedule schedule, boolean isAssetFinished) {
        String assetId = dto.getAssetId();
        String targetItem = dto.getTargetItem();
        String targetState = dto.getInspectState();
        int stateInt = Integer.parseInt(targetState);

        String eventTypeId = dto.getEventTypeId();
        // eventTypeId 为空情况
        // 1. 采集器没有返回，指标采集项目为空（一般是采集器得数据关联有问题）
        //    目前为空 推送正常（非设备真实异常）
        // 2. 采集结束
        if (null == eventTypeId) {
            String assetName = session.getAssetIdNameMap().get(assetId);
            Map<String, Integer> assetStateMap = session.getAssetStateMap();
            Integer statusCode = Integer.valueOf(InspectionStatus.INSPECTED.getCode());
            // 主要针对当前巡检设备未配置指标（特殊情况）
            if (!(null == assetStateMap || assetStateMap.isEmpty())) {
                statusCode = assetStateMap.get(assetId);
                if (null == statusCode) {
                    statusCode = Integer.valueOf(InspectionStatus.INSPECTED.getCode());
                }
            }
            try {
                session.updateLatestAsset(assetId, assetName, statusCode);
            } catch (Exception ignored) {
            }
            InspectBaseDataWsVo snapshot = session.buildSnapshot(schedule.getJobId(), false, isAssetFinished);
            notifier.sendSnapshot(snapshot, schedule.getOperator());

            // 异步保存详情
            saveDetailAsync(dto, session.getAssetList(), true);
            return;
        }

        // 指标项去重
        String uniqueKey = assetId + "_" + targetItem;
        if (!session.getProcessedUniqueItems().add(uniqueKey)) {
            return;
        }

        // 更新资产状态
        updateAssetState(session, assetId, stateInt, targetItem);

        // 异常判断
        boolean isAbnormal = InspectionStatus.INSPECT_ALARM.getCode().equals(targetState);

        // 异步保存详情
        saveDetailAsync(dto, session.getAssetList(), false);

        // 更新最新上下文（用于快照）
        String assetName = session.getAssetIdNameMap().get(assetId);
        // 更新大于指标得 - 资产状态
        session.updateLatestAsset(assetId, assetName, session.getAssetStateMap().get(assetId));
        // 更新指标状态
        session.updateLatestTarget(eventTypeId, isAbnormal);

        // 推送快照！
        InspectBaseDataWsVo snapshot = session.buildSnapshot(schedule.getJobId(), true, isAssetFinished);
        notifier.sendSnapshot(snapshot, schedule.getOperator());
    }

    private void updateAssetState(InspectSession session, String assetId, int newState, String targetItem) {
        session.getAssetStateMap().compute(assetId, (k, oldState) -> {
            if (oldState == null) {
                return newState;
            }
            return Math.max(oldState, newState); // 取更严重的状态
        });
    }

    @Async("xunjianAsync")
    public void saveDetailAsync(XunjianDataDto dto, List<InspectAsset> assetList, boolean isUpdate) {
        for (InspectAsset asset : assetList) {
            if (asset.getAssetId().equals(dto.getAssetId()) && asset.getTargetItem().equals(dto.getTargetItem())) {
                asset.setInspectState(dto.getInspectState());
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
                inspectDetail.setAlarmId(dto.getAlarmId());
                inspectDetail.setInspectValue(dto.getInspectValue());
                inspectDetail.setResultMsg(dto.getResultMsg());
                inspectDetailService.save(inspectDetail);
                break;
            }
            // 特殊情况，如果当前设备采集得指标为空，那么还需要将数据库得设备状态设置完毕
            if (asset.getAssetId().equals(dto.getAssetId()) && isUpdate) {
                InspectAsset byId = inspectAssetService.getById(asset.getId());
                if (null == byId.getInspectState() || Integer.parseInt(byId.getInspectState()) < Integer.parseInt(dto.getInspectState())) {
                    asset.setInspectState(dto.getInspectState());
                    asset.setInspectValue(dto.getInspectValue());
                    asset.setResultMsg(dto.getResultMsg());
                    inspectAssetService.updateById(asset);
                }
            }
        }
    }
}
