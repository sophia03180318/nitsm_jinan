package com.jcca.common.enums;

import com.jcca.common.bean.constant.StatusConst;
import lombok.Getter;

/**
 * 数据状态枚举-用于逻辑删除控制
 *
 * @author hanwone
 * @date 2018/8/14
 */
@Getter
public enum StatusEnum {

    /**
     * 不正常的数据
     */
    NO(StatusConst.NO, "不正常"),
    /**
     * 正常的数据
     */
    OK(StatusConst.OK, "正常"),
    /**
     * 被冻结的数据，不可用
     */
    FREEZED(StatusConst.FREEZED, "冻结"),
    /**
     * 数据已被删除
     */
    DELETE(StatusConst.DELETE, "删除");

    private Byte code;

    private String message;

    StatusEnum(Byte code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMsgByCode(Byte code) {
        StatusEnum[] values = StatusEnum.values();
        for (StatusEnum statusEnum : values) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum.getMessage();
            }
        }
        return code.toString();
    }
}

