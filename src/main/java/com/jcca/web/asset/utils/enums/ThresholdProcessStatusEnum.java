package com.jcca.web.asset.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author syt
 * @date 2021/08/24  10:39
 * @classname nitsmcom.jcca.web.asset.utils.enumsThresholdProcessStatusEnum
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ThresholdProcessStatusEnum {

    NORMAL(1, "正常"),
    ABNORMAL(0, "不正常");

    private Integer code;
    private String name;
}
