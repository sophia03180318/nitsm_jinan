package com.jcca.common.enums;

import lombok.Getter;

/**
 * 告警类型
 *
 * @author Lvyp
 */
@Getter
public enum AlarmTypeEnum {

    HARDWARE(1, "硬件告警"),

    SOFTWARE(2, "软件告警");

    private Integer code;
    private String msg;

    private AlarmTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsg(Integer code) {
        AlarmTypeEnum[] values = AlarmTypeEnum.values();
        for (AlarmTypeEnum item : values) {
            if (item.getCode().equals(code)) {
                return item.getMsg();
            }
        }
        return code + "";
    }

}
