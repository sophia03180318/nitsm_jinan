package com.jcca.web.event.enums;

import lombok.Getter;

/**
 * 事件组逻辑过膝
 *
 * @author lyp
 */
@Getter
public enum EventGroupLogicEnum {

    AND(1, "与"),

    OR(2, "或");

    private Integer code;
    private String msg;

    private EventGroupLogicEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsgByCode(Integer code) {
        EventGroupLogicEnum[] values = EventGroupLogicEnum.values();
        for (EventGroupLogicEnum eventGroupLogicEnum : values) {
            if (eventGroupLogicEnum.getCode().equals(code)) {
                return eventGroupLogicEnum.getMsg();
            }
        }
        return "";
    }

}
