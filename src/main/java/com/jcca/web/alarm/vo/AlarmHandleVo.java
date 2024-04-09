package com.jcca.web.alarm.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @ClassName AlarmHandleVo
 * @Description 处理告警
 * @Date 2020/6/17 17:08
 * @Author hanwone
 */
@Data
public class AlarmHandleVo {
    /**
     * 告警ID
     */
    @NotNull(message = "ID不能为空")
    private String id;
    /**
     * 备注
     */
    private String remark;
}
