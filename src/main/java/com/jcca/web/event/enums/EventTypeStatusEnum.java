package com.jcca.web.event.enums;

import lombok.Getter;

/**
 * 状态
 *
 * @author lyp
 */
@Getter
public enum EventTypeStatusEnum {

    USED(1, "使用中"),

    DELETE(-1, "已删除");

    private Integer code;
    private String msg;

    private EventTypeStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
