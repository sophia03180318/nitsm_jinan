package com.jcca.component.crsc.enums;

/**
 * @Description 通号进程状态枚举
 * @ClassName ProcessStateEnum
 * @Date 2022/5/9 16:50
 * @Author hanwone
 * @Since 2.0.0.1
 */
public enum ProcessStateEnum {

    RUN(1, "启动"),
    STOP(2, "停止"),
    ALARM(3, "报警"),
    UNKNOW(-1, "未知"),
    ;

    int code;
    String name;

    ProcessStateEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(int code) {
        ProcessStateEnum[] values = ProcessStateEnum.values();
        for (ProcessStateEnum value : values) {
            if (code == value.code) {
                return value.name;
            }
        }
        return "";
    }
}
