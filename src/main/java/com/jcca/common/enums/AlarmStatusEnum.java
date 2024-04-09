package com.jcca.common.enums;

import lombok.Getter;

/**
 * 告警确认状态
 *
 * @author lyp
 */
@Getter
public enum AlarmStatusEnum {
    /**
     * 未确认
     */
    UNCONFIRM((byte) 1, "未确认"),
    /**
     * 已确认
     */
    CONFIRMED((byte) 2, "已确认");

    private Byte code;
    private String msg;

    AlarmStatusEnum(Byte code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsg(Byte code) {
        AlarmStatusEnum[] values = AlarmStatusEnum.values();
        for (AlarmStatusEnum item : values) {
            if (item.getCode() == code) {
                return item.getMsg();
            }
        }
        return code + "";
    }

}
