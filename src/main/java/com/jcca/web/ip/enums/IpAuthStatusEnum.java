package com.jcca.web.ip.enums;

import lombok.Getter;

/**
 * ip审核状态
 *
 * @author Lvyp
 */
@Getter
public enum IpAuthStatusEnum {

    NO(0, "未申请"),
    YES(1, "已申请");

    private Integer code;
    private String msg;

    private IpAuthStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsgByCode(Integer authStatus) {
        IpAuthStatusEnum[] values = IpAuthStatusEnum.values();
        for (IpAuthStatusEnum item : values) {
            if (item.getCode().equals(authStatus)) {
                return item.getMsg();
            }
        }
        return authStatus + "";
    }


}
