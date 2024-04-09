package com.jcca.common.enums;

import lombok.Getter;

/**
 * @ClassName AlarmStateEnum
 * @Description 告警状态
 * @Date 2020/6/24 10:54
 * @Author hanwone
 */
@Getter
public enum AlarmStateEnum {
    /**
     * 告警
     */
    ALARM((byte) 1, "告警"),
    /**
     * 恢复
     */
    RECOVER((byte) 2, "恢复");


    private Byte code;
    private String msg;

    AlarmStateEnum(Byte code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 是否是告警
     *
     * @param code
     * @return
     */
    public static Boolean isAlarm(Byte code) {
        return ALARM.getCode() == code;
    }

    public static String getMsg(Byte code) {
        AlarmStateEnum[] values = AlarmStateEnum.values();
        for (AlarmStateEnum stateEnum : values) {
            if (code.byteValue() == stateEnum.code) {
                return stateEnum.msg;
            }
        }
        return "-";
    }
}
