package com.jcca.web.ip.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * ip状态
 *
 * @author lyp
 */
@Getter
public enum IpStatusEnum {
    /**
     * 已经使用(IP上有录入资产)
     */
    MONITOR((byte) 1, "已使用"),
    /**
     * 未使用(IP上未录入资产)
     */
    ALLOT((byte) 2, "未使用");

    private Byte code;
    private String msg;

    private IpStatusEnum(Byte code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static final String getMsgByCode(Byte code) {
        if (Objects.isNull(code)) {
            return "";
        }
        IpStatusEnum[] values = IpStatusEnum.values();
        for (IpStatusEnum item : values) {
            if (item.getCode().equals(code)) {
                return item.getMsg();
            }
        }
        return code + "";
    }

}
