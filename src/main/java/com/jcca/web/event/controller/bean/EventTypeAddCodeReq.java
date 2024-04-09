package com.jcca.web.event.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 添加单个匹配码到已有事件中
 *
 * @author lyp
 */
@Data
public class EventTypeAddCodeReq {

    /**
     * 已有类型ID
     */
    @NotEmpty(message = "类型ID不可空")
    private String typeId;
    /**
     * 匹配code
     */
    @NotEmpty(message = "事件匹配码不可空")
    private String code;

}
