package com.jcca.web.ip.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * ip ping状态
 *
 * @author lyp
 */
@Getter
public enum IpPingStatusEnum {

    /**
     * IP通
     */
    USED((byte) 1, "ip通"),
    /**
     * IP不通
     */
    UNUSED((byte) 0, "ip不通"),

    /**
     * ping通(实际意义上)
     */
    PING_YES((byte) 2, "通"),
    /**
     * ping不通(实际意义上)
     */
    PING_NO((byte) 3, "断");

    private Byte code;

    private String msg;

    private IpPingStatusEnum(Byte code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsgByCode(Byte code) {
        if (Objects.isNull(code)) {
            return "";
        }
        IpPingStatusEnum[] values = IpPingStatusEnum.values();
        for (IpPingStatusEnum item : values) {
            if (item.getCode().equals(code)) {
                return item.getMsg();
            }
        }
        return code + "";
    }

}
