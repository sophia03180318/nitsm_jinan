package com.jcca.web.event.controller.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * 事件更新
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventGroupUpdateReq extends EventGroupAddReq {

    @NotEmpty(message = "id不可空")
    private String id;

}
