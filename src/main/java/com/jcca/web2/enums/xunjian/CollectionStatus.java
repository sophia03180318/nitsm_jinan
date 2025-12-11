package com.jcca.web2.enums.xunjian;


import lombok.Getter;

import java.util.Objects;

/**
 * @author lifp
 * @version 1.0
 * @description: 采集状态 具体说明请见
 * <p><a href="http://192.168.73.199:8090/pages/viewpage.action?pageId=72221212">具体说明</a></p>
 * @date 2025-12-05 星期五 13:52:25
 */
@Getter
public enum CollectionStatus {
    IS_SUCCESS(1, "采回数据并判断结果是正常还是异常"),
    IS_ERROR(2, "直接判断是异常"),
    IS_FAIL(3, "因为采集数据异常捯饬的错误码"),
    IS_FAIL_DOUBLE(4, "因为采集数据异常捯饬的错误码");

    private final int code;
    private final String description;

    CollectionStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static CollectionStatus fromCode(int code) {
        for (CollectionStatus status : values()) {
            if (Objects.equals(status.code, code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid inspection status code: " + code);
    }
}
