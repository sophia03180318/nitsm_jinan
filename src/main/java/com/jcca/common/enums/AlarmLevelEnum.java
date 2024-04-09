package com.jcca.common.enums;

import com.jcca.common.bean.constant.AlarmLevelConst;
import lombok.Getter;

/**
 * 告警级别枚举
 *
 * @author hanwone
 */
@Getter
public enum AlarmLevelEnum {
    /**
     * 未知告警
     */
    UNKNOW(AlarmLevelConst.UNKNOW, "未知告警"),
    LEVEL_ONE(AlarmLevelConst.SERIOUS_LEVEL, "一级告警"),
    LEVEL_TWO(AlarmLevelConst.MIDDLE_LEVEL, "二级告警"),
    LEVEL_THREE(AlarmLevelConst.LIGHT_LEVEL, "三级告警"),
    LEVEL_MSG(AlarmLevelConst.MSG_LEVEL, "信息通知");

    private Byte code;

    private String msg;

    AlarmLevelEnum(byte code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsg(Byte code) {
        AlarmLevelEnum[] values = AlarmLevelEnum.values();
        for (AlarmLevelEnum item : values) {
            Byte code2 = item.getCode();
            if (code2.equals(code)) {
                return item.getMsg();
            }
        }
        return code + "";
    }

}
