package com.jcca.web2.enums.xunjian;


import lombok.Getter;

/**
 * @author lifp
 * @version 1.0
 * @description: TODO
 * @date 2025-12-05 星期五 14:01:10
 */
@Getter
public enum InspectionMode {
    MANUAL(1, "手动巡检"),
    SCHEDULED(2, "周期巡检");

    private final int code;
    private final String description;

    InspectionMode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static InspectionMode fromCode(int code) {
        for (InspectionMode mode : values()) {
            if (mode.code == code) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid inspection mode code: " + code);
    }
}
