package com.jcca.web.alarm.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 增加告警处理意见
 *
 * @author Lvyp
 */
@Data
public class AddOpinionReq implements Serializable {


    private static final long serialVersionUID = 1L;
    /**
     * 告警id
     */
    @NotEmpty(message = "告警ID不能空")
    private String id;
    /**
     * 备注
     */
    private String remark;
}
