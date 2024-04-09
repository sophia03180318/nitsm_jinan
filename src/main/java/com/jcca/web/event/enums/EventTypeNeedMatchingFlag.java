package com.jcca.web.event.enums;

import lombok.Getter;

/**
 * 类型是否需要匹配告警标识标记
 *
 * @author lyp
 */
@Getter
public enum EventTypeNeedMatchingFlag {

    NEED(1, "需要"),

    DONT(-1, "不需要");

    private Integer code;
    private String msg;

    private EventTypeNeedMatchingFlag(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsgByCode(Integer code) {
        EventTypeNeedMatchingFlag[] values = EventTypeNeedMatchingFlag.values();
        for (EventTypeNeedMatchingFlag item : values) {
            if (item.getCode().equals(code)) {
                return item.getMsg();
            }
        }

        return "";
    }

}
