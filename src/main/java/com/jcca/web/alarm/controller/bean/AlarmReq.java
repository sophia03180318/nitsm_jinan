package com.jcca.web.alarm.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Description 资产备注
 * @Author sophia
 */
@Data
public class AlarmReq {

    @NotNull(message = "资产ID不能为空")
    private String alarmId;

    private String remark;
}
