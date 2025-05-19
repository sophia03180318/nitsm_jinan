package com.jcca.web.event.controller.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

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
    /**
     * 设备类型，多个用英文逗号隔开
     */
    private String assetDesks;
    /**
     * 事件类型别名
     */
    @Length(max = 64)
    private String typeAlias;
}
