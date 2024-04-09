package com.jcca.web.event.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 事件性质
 *
 * @author lyp
 */
@Getter
public enum EventLevelEnum {
    /**
     * 异常事件
     */
    ABNORMAL(-1, "异常"),
    /**
     * 恢复事件
     */
    NORMAL(1, "正常"),
    /**
     * 警示事件 重要等级低于异常  高于通知
     */
    @Deprecated
    WARNING(2, "警告"),
    /**
     * 通知事件
     */
    @Deprecated
    NOTIFY(0, "通知");

    private Integer code;
    private String msg;


    /**
     * 是否是告警
     *
     * @param level
     * @return
     */
    public static Boolean isAlarm(Integer level) {
        List<Integer> alarmEventList = Arrays.asList(ABNORMAL.getCode(), WARNING.getCode(), NOTIFY.getCode());
        if (alarmEventList.contains(level)) {
            return true;
        }
        return false;
    }

    private EventLevelEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsgByCode(Integer code) {
        EventLevelEnum[] values = EventLevelEnum.values();
        for (EventLevelEnum eventLevelEnum : values) {
            if (eventLevelEnum.getCode().equals(code)) {
                return eventLevelEnum.getMsg();
            }
        }

        return "";
    }

    public static EventLevelEnum getEnumByCode(Integer code) {
        EventLevelEnum[] values = EventLevelEnum.values();
        for (EventLevelEnum eventLevelEnum : values) {
            if (eventLevelEnum.getCode().equals(code)) {
                return eventLevelEnum;
            }
        }

        return null;
    }

}
