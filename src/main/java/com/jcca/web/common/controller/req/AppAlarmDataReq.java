package com.jcca.web.common.controller.req;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;


/**
 * APP告警回传参数
 *
 * @author lyp
 */
@Data
public class AppAlarmDataReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "告警ID不可空")
    private String alarmId;

    private String remark;

    @NotEmpty(message = "请传入确定人")
    private String confirmor;

}
