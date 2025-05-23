package com.jcca.web2.dto;

import lombok.Data;

/**
 * @author: hhw
 * @description: InspectTargetDetailInfo 主要是用来
 * @date: 2025-05-23  09:43
 * @since: 2.1.6.0
 */
@Data
public class InspectTargetDetailInfo {

    private String inspectCode;
    private String assetId;

    private String targetItem;
    private String targetName;
    private String inspectState;
    /**
     * 设定阈值
     */
    private String thresholdValue;
    /**
     * 采集值
     */
    private String inspectValue;
    /**
     * 巡检结果说明
     */
    private String resultMsg;
    /**
     * 指导意见
     */
    private String remark;
}
