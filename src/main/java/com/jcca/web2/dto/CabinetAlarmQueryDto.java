package com.jcca.web2.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @description: 查询机柜告警列表的请求参数
 * @author: Lvyp
 * @create: 2023/11/01 14:24
 */
@Data
public class CabinetAlarmQueryDto {

    /**
     * 机柜ID
     */
    @NotEmpty(message = "机柜ID不能为空")
    private String cabinetId;
    /**
     * 确认状态
     * 1未确认  2已确认
     */
    private String status;
    /**
     * 告警状态
     * 1告警  2恢复
     */
    private String alarmState;
}
