package com.jcca.web2.dto.xunjian;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author lifp
 * @version 1.0
 * @description: 智能巡检报告单 - 告警详情
 * @date 2025-10-09 星期四 15:28:37
 */
@Data
public class InspectEventDetail {

    @NotNull(message = "id 不可空")
    private Long id;
    /**
     * 资产的ID
     */
    @NotEmpty(message = "资产ID不可空")
    private String assetId;
    /**
     * 告警ID
     */
    @NotEmpty(message = "告警ID")
    private String alarmId;
    /**
     * 告警的标题
     */
    @NotEmpty(message = "告警标题不可空")
    private String alarmTitle;
    /**
     * 设备名称
     */
    @NotEmpty(message = "设备名称")
    private String assetName;
    /**
     * 设备类型
     */
    @NotEmpty(message = "设备类型")
    private String assetMode;
    /**
     * 告警的级别
     */
    @NotNull(message = "告警级别不可空")
    private Integer alarmLevel;
    /**
     * 处理状态
     */
    @NotNull(message = "处理状态")
    private Integer solveStatus;

    /**
     * 告警描述
     */
    @NotEmpty(message = "告警信息不能空")
    private String alarmDescription;

    /**
     * 参考建议
     */
    @NotEmpty(message = "参考建议")
    private String remarkStr;
}
