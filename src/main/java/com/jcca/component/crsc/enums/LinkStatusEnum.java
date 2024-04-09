package com.jcca.component.crsc.enums;

/**
 * @Description 通号进程连接状态枚举
 * @ClassName LinkStateEnum
 * @Date 2022/5/11 13:15
 * @Author hanwone
 * @Since 2.0.0.1
 */
public enum LinkStatusEnum {
    UP("UP", "通"),
    DOWN("DOWN", "断"),
    DEFNULL("DEFNULL", "无效"),
    ;

    String code;
    String name;

    LinkStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(String code) {
        LinkStatusEnum[] values = LinkStatusEnum.values();
        for (LinkStatusEnum value : values) {
            if (code.equals(value.code)) {
                return value.name;
            }
        }
        return "";
    }
}
