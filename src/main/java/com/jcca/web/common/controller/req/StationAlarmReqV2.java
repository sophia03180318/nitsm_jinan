package com.jcca.web.common.controller.req;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @description: 车站告警
 * @author: Lvyp
 * @create: 2024/04/28 17:51
 */
@Data
public class StationAlarmReqV2 {

    @NotNull(message = "id 不可空")
    private Long id;
    /**
     * 资产的ID
     */
    @NotEmpty(message = "资产ID不可空")
    private String assetId;
    /**
     * 告警的标题
     */
    @NotEmpty(message = "告警标题不可空")
    private String alarmTitle;
    /**
     * 告警的级别
     */
    @NotNull(message = "告警级别不可空")
    private Integer alarmLevel;
    /**
     * 告警确认状态
     * 1 未确认  2 已确认
     */
    @NotNull(message = "告警状态不可空")
    private Integer alarmStatus;
    /**
     * 告警恢复状态
     * 1 告警
     * 2 恢复
     */
    @NotNull(message = "恢复状态不可空")
    private Integer alarmRecoverStatus;
    /**
     * 告警编号
     */
    @NotEmpty(message = "告警编号不能空")
    private String alarmCode;
    /**
     * 告警描述
     */
    @NotEmpty(message = "告警信息不能空")
    private String alarmDescription;
    /**
     * 告警产生时间
     */
    @NotNull(message = "产生时间不能空")
    private Date occurTime;
    /**
     * 告警产生时间 yyyyMMddHHmmss
     */
    private String occurTimeStr;
    /**
     * 标记
     */
    private String flag;

    /**
     * AlarmTypeEnum
     * 硬件软件告警
     */
    private Integer alarmType;

    /**
     * 此种类型告警的唯一标识码
     * 后续ITSM使用
     */
    private String uniqueCode;

}
