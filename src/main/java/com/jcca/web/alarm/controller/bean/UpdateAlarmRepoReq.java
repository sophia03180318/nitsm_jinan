package com.jcca.web.alarm.controller.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 新增告警知识库
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateAlarmRepoReq extends AddAlarmRepoReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "参数缺少ID")
    private String id;


}
