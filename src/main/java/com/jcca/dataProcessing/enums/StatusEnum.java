package com.jcca.dataProcessing.enums;

import lombok.Getter;

/**
 * 枚举
 *
 * @author zhaozheng
 */
@Getter
public enum StatusEnum {


    /**
     * 网卡状态信息
     * // 1:通、2：断、3：在测试模式下、4：未知、5：休眠 6：缺少组件 7：DOWN_DUE_TO_STATE_OF 由于状态而向下
     */
    status_net_1("1", "通"),
    status_net_2("2", "断"),
    status_net_3("3", "在测试模式下"),
    status_net_4("4", "未知"),
    status_net_5("5", "休眠"),
    status_net_6("6", "缺少组件"),
    status_net_7("7", "DOWN_DUE_TO_STATE_OF 由于状态而向下");


    private String code;
    private String name;

    StatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(String code) {
        StatusEnum[] values = StatusEnum.values();
        for (StatusEnum value : values) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return "UNKNOWN";
    }
}
