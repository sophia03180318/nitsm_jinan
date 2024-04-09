package com.jcca.web.event.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 新增事件类型
 *
 * @author lyp
 */
@Data
public class EventTypeAddReq {

    /**
     * 事件类型名称
     */
    @NotEmpty(message = "请输入事件类型名称")
    private String name;
    /**
     * 类型描述
     */
    private String descStr;

}
