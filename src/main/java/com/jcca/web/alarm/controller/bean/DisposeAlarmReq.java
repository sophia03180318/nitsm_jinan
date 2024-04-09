package com.jcca.web.alarm.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 告警处理
 *
 * @author Lvyp
 */
@Data
public class DisposeAlarmReq {

    /**
     * 告警ID集合
     */
    @NotNull(message = "请输入处理的告警ID")
    @Size(min = 1, message = "请输入处理的告警ID")
    private List<String> idList;
    /**
     * 备注
     */
    private String remark;

}
