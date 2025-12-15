package com.jcca.web2.dto.xunjian;

import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.entity.XunjianSchedule;
import com.jcca.web2.enums.xunjian.InspectionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 巡检会话上下文（每个巡检任务唯一）
 * <p>
 * 负责：
 * - 存储任务元数据（schedule、资产列表）
 * - 聚合实时状态（资产状态、指标状态、进度）
 * - 去重与计数
 * - 构建 WebSocket 快照
 * <p>
 * 注意：该对象必须在任务结束或异常时显式销毁，防止内存泄漏！
 *
 * @author lifp
 * @version 1.0
 * @description: 巡检上下文
 * @date 2025-12-04 星期四 13:54:58
 */
@Data
public class InspectSession {

    private final String inspectRecordId;
    private final XunjianSchedule schedule;
    private final List<InspectAsset> assetList;


    /**
     * 资产最终聚合状态（取最严重状态）
     */
    private final Map<String, Integer> assetStateMap = new ConcurrentHashMap<>();

    /**
     * 已完成的资产ID集合
     */
    private final Set<String> completedAssetIds = ConcurrentHashMap.newKeySet();

    /**
     * assetId -> assetName 映射
     */
    private final Map<String, String> assetIdNameMap = new HashMap<>();
    /**
     * targetItem -> targetName 白名单（用于过滤非法指标）
     */
    private final Map<String, String> allowedTargetItems = new HashMap<>();


    /**
     * 资产总数（去重后）
     */
    private final int assetTotal;

    // 最新上下文（用于快照）
    private volatile String latestAssetId;
    private volatile String latestAssetName;
    private volatile int latestAssetStatus;

    private volatile String latestEventTypeId;
    private volatile boolean latestIsAbnormal;  // 最新指标是否异常

    /**
     * 更新最新巡检资产上下文（用于快照）
     */
    public void updateLatestAsset(String assetId, String assetName, int status) {
        this.latestAssetId = assetId;
        this.latestAssetName = assetName;
        this.latestAssetStatus = status;
    }

    /**
     * 更新最新指标上下文（用于快照）
     */
    public void updateLatestTarget(String eventTypeId, boolean isAbnormal) {
        this.latestEventTypeId = eventTypeId;
        this.latestIsAbnormal = isAbnormal;
    }

    /**
     * 构造函数：初始化静态映射与计数
     */
    public InspectSession(String inspectRecordId, XunjianSchedule schedule, List<InspectAsset> assetList) {
        this.inspectRecordId = inspectRecordId;
        this.schedule = schedule;
        this.assetList = assetList;

        // 按 assetId 分组，计算资产总数
        Map<String, List<InspectAsset>> groupedByAsset = assetList.stream()
                .collect(Collectors.groupingBy(InspectAsset::getAssetId));
        this.assetTotal = groupedByAsset.size();

        for (InspectAsset a : assetList) {
            allowedTargetItems.put(a.getTargetItem(), a.getTargetName());
            assetIdNameMap.put(a.getAssetId(), a.getAssetName());
        }

    }

    /**
     * 获取当前巡检进度（按资产完成数计算）
     */
    public int getProgress() {
        if (assetTotal == 0) return 0;
        int completed = completedAssetIds.size();
        BigDecimal progress = new BigDecimal(completed)
                .divide(new BigDecimal(assetTotal), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return progress.intValue();
    }

    /**
     * 标记资产为已完成，并触发最终状态更新（调用方需异步更新DB）
     */
    public void markAssetAsCompleted(String assetId) {
        this.completedAssetIds.add(assetId);
    }

    /**
     * 构建 WebSocket 快照对象
     *
     * @param jobId  任务ID
     * @param isPush 是否推送指标
     * @return 快照VO
     */
    public InspectBaseDataWsVo buildSnapshot(String jobId, boolean isPush, boolean isAssetFinished) {
        InspectBaseDataWsVo vo = new InspectBaseDataWsVo();
        vo.setProgress(getProgress());
        vo.setMsgType(XunjianWSDto.ASSET_STATUS);
        vo.setJobId(jobId);
        // 当前巡检设备
        if (latestAssetId != null) {
            InspectBaseDataWsVo.CurrentAsset current = new InspectBaseDataWsVo.CurrentAsset();
            current.setId(latestAssetId);
            current.setName(latestAssetName);
            current.setStatus(latestAssetStatus);
            vo.setCurrentInspectingDevice(current);
        }

        InspectBaseDataWsVo.AssetState assetState = new InspectBaseDataWsVo.AssetState();
        assetState.setId(latestAssetId);
        assetState.setName(latestAssetName);
        // 资产状态
        if (latestAssetId != null && isAssetFinished) {
            assetState.setStatus(latestAssetStatus);
        } else {
            // 只要非完成，都是巡检中
            assetState.setStatus(Integer.parseInt(InspectionStatus.INSPECTING.getCode()));
        }
        vo.setAssetData(assetState);

        InspectBaseDataWsVo.TargetAbnormal target = new InspectBaseDataWsVo.TargetAbnormal();
        target.setId(latestEventTypeId);
        // 指标异常
        if (latestEventTypeId != null && isPush) {
            // 1 异常  0 正常
            target.setAbnormal(latestIsAbnormal ? 1 : 0);
        }
        vo.setTargetData(target);
        return vo;
    }
}
