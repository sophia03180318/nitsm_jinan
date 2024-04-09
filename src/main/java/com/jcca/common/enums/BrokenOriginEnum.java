package com.jcca.common.enums;

import lombok.Getter;

/**
 * 故障来源
 *
 * @author lyp
 */
@Getter
public enum BrokenOriginEnum {

    MANUAL_WORK((byte) 1, "手工录入"),

    SYSTEM_ALARM((byte) 2, "告警转换");

    private Byte code;

    private String msg;

    BrokenOriginEnum(byte code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsg(Byte code) {
        BrokenOriginEnum[] values = BrokenOriginEnum.values();
        for (BrokenOriginEnum item : values) {
            if (item.getCode().equals(code)) {
                return item.getMsg();
            }
        }
        return code + "";
    }

}
