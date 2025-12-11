package com.jcca.web2.dto.xunjian;


import lombok.Data;

/**
 * @author lifp
 * @version 1.0
 * @description= TODO
 * @date 2025-12-04 星期四 10=50=42
 */
@Data
public class InspectBaseDataWsVo {

    /**
     * 已巡检异常指标数量
     */
    private TargetAbnormal targetData;
    /**
     * 资产状态
     */
    private AssetState assetData;

    /**
     * 巡检中得状态
     */
    private CurrentAsset currentInspectingDevice;

    /**
     * 进度条
     */
    private int progress;

    /**
     * 消息类型
     */
    private int msgType;

    // 任务id
    private String jobId;

    @Data
    public static class TargetAbnormal {
        private int abnormal;
        // 指标id
        private String id;
    }

    @Data
    public static class AssetState {
        private int status;
        // 资产id
        private String id;
        // 资产名称
        private String name;
    }

    @Data
    public static class CurrentAsset {
        // 资产id
        private String id;
        private int status;
        // 资产名称
        private String name;
    }
}
