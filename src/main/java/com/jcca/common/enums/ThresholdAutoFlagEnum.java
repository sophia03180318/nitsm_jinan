package com.jcca.common.enums;

import com.jcca.common.bean.constant.ThresholdAutoFlagConst;
import lombok.Getter;

import java.util.Objects;

/**
 * @ClassName ThresholdAutoFlagEnum
 * @Description 设备阈值设置状态
 * @Date 2020/8/26 9:34
 * @Author hanwone
 */
@Getter
public enum ThresholdAutoFlagEnum {

    ORG_THRESHOLD(ThresholdAutoFlagConst.ORG_THRESHOLD, "批量阈值"),

    MODE_THRESHOLD(ThresholdAutoFlagConst.MODE_THRESHOLD, "类型阈值"),

    ASSET_THRESHOLD(ThresholdAutoFlagConst.SIGNLE_THRESHOLD, "单独阈值"),

    ;

    private byte code;
    private String name;

    ThresholdAutoFlagEnum(Byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(Byte code) {
        if (Objects.nonNull(code)) {
            ThresholdAutoFlagEnum[] values = ThresholdAutoFlagEnum.values();
            for (ThresholdAutoFlagEnum value : values) {
                if (value.code == code) {
                    return value.name;
                }
            }
        }
        return "";
    }
}
