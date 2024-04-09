package com.jcca.web.collect.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 传感
 *
 * @author Lvyp
 */
@Getter
public enum SensorStatusEnum {
    /**
     * 传感未打开
     */
    NOT_OPEN("0", "正常"),
    /**
     * 正常
     */
    NORMAL("1", "正常"),
    /**
     * 不正常
     */
    UNNORMAL("2", "不正常");

    private String code;
    private String msg;

    private SensorStatusEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取状态
     *
     * @param switchStatus
     * @param status
     * @return
     */
    public static String getSensorStatus(String switchStatus, String status) {
        if (StrUtil.isEmpty(switchStatus) || StrUtil.isEmpty(status)) {
            return null;
        }
        if (!switchStatus.contains("1")) {
            return NOT_OPEN.code;
        }
        List<String> normalList = Arrays.asList("NORMAL", "GREEN");
        if (normalList.contains(status.toUpperCase())) {
            return NORMAL.code;
        }
        return UNNORMAL.getCode();
    }

    public static String getMsgByCode(String code) {
        SensorStatusEnum[] values = SensorStatusEnum.values();
        for (SensorStatusEnum item : values) {
            if (item.getCode().equals(code)) {
                return item.getMsg();
            }
        }

        return "--";
    }

}
