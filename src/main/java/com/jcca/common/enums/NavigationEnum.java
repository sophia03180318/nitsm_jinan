package com.jcca.common.enums;

import lombok.Getter;

/**
 * 导航栏枚举
 *
 * @author hanwone
 */
@Getter
public enum NavigationEnum {

    INDEX(0, "首页"),
    CABINET(1, "机柜"),
    ASSET(2, "资产"),
    DETAIL(3, "详情");

    private int code;

    private String description;

    NavigationEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
