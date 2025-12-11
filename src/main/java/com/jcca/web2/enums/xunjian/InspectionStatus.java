package com.jcca.web2.enums.xunjian;


import lombok.Getter;

import java.util.Objects;

/**
 * @author lifp
 * @version 1.0
 * @description: TODO
 * @date 2025-12-05 星期五 13:52:25
 */
@Getter
public enum InspectionStatus {
    UNKNOWN("0", "未知"),
    INSPECT("1", "待巡检"),
    INSPECTING("2", "正在巡检"),
    INSPECTED("3", "巡检正常"),
    INSPECT_ERROR("4", "巡检异常"),
    INSPECT_ALARM("5", "巡检告警");

    private final String code;
    private final String description;

    InspectionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static InspectionStatus fromCode(String code) {
        for (InspectionStatus status : values()) {
            if (Objects.equals(status.code, code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid inspection status code: " + code);
    }
}
