package com.jcca.web.event.controller.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * 更新事件类型
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventTypeUpdateReq extends EventTypeAddReq {

    @NotEmpty(message = "请选择需要修改的记录")
    private String id;

}
