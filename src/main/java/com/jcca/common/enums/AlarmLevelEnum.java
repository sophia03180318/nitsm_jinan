package com.jcca.common.enums;

import com.jcca.common.bean.constant.AlarmLevelConst;
import lombok.Getter;

/**
 * 告警级别枚举
 * 对应字典：ALARM_LEVEL
 * @author hanwone
 */
@Getter
public enum AlarmLevelEnum {
    /**
     *
     */
    LEVEL_ONE(AlarmLevelConst.SERIOUS_LEVEL, "一级告警"),
    LEVEL_TWO(AlarmLevelConst.MIDDLE_LEVEL, "二级告警"),
    LEVEL_THREE(AlarmLevelConst.LIGHT_LEVEL, "三级告警"),
    LEVEL_MSG(AlarmLevelConst.MSG_LEVEL, "信息通知"),
    UN_CONFIG(AlarmLevelConst.UN_CONFIG, "未配制");

    private Integer code;

    private String msg;

    AlarmLevelEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsg(Byte code) {
        AlarmLevelEnum[] values = AlarmLevelEnum.values();
        for (AlarmLevelEnum item : values) {
            Integer code2 = item.getCode();
            if (code2.equals(code)) {
                return item.getMsg();
            }
        }
        return code + "";
    }

}
