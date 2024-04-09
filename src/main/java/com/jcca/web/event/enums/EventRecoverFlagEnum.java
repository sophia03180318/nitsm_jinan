package com.jcca.web.event.enums;

import lombok.Getter;

@Getter
public enum EventRecoverFlagEnum {

    CAN(1, "可恢复"),

    CANT(-1, "不可恢复");

    private Integer code;
    private String msg;

    private EventRecoverFlagEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsgByCode(Integer code) {
        EventRecoverFlagEnum[] values = EventRecoverFlagEnum.values();
        for (EventRecoverFlagEnum eventRecoverFlagEnum : values) {
            if (eventRecoverFlagEnum.getCode().equals(code)) {
                return eventRecoverFlagEnum.getMsg();
            }
        }
        return "";
    }

}
