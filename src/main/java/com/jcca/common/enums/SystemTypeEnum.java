package com.jcca.common.enums;

import lombok.Getter;

/**
 * 采集类型
 *
 * @author Lvyp
 */
@Getter
public enum SystemTypeEnum {

    SWITCH_ROUTER(-1, "switch_router"),
    LINUX(0, "linux"),
    WINDOWS(1, "windows"),
    AIX(2, "aix"),
    DOS(3, "dos"),
    MAC_OSX(4, "macos");

    private Integer code;
    private String name;

    SystemTypeEnum(Integer code, String name) {
        this.name = name;
        this.code = code;
    }

    public static String getName(int code) {
        SystemTypeEnum[] values = SystemTypeEnum.values();
        for (SystemTypeEnum value : values) {
            if (value.code == code) {
                return value.name;
            }
        }
        return "UNKNOWN";
    }

}
