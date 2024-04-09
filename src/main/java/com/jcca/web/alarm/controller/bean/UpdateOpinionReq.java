package com.jcca.web.alarm.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 更新历史处理意见
 *
 * @author Lvyp
 */
@Data
public class UpdateOpinionReq implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 告警id
     */
    @NotEmpty(message = "告警ID不能空")
    private String alarmId;
    /**
     * 意见id
     */
    @NotEmpty(message = "意见ID不能空")
    private String opinionId;
    /**
     * 修改的意见
     */
    @NotEmpty(message = "修改的建议不能空")
    private String opinion;

}
