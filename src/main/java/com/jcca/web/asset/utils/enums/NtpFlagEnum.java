package com.jcca.web.asset.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author syt
 * @date 2021/06/09  17:51
 * @classname nitsmcom.jcca.web.asset.utils.enumsNtpFlagEnum
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum NtpFlagEnum {

    NTP_YES(Byte.valueOf("1"), "采集"),

    NTP_NO(Byte.valueOf("0"), "不采集");

    private Byte code;
    private String msg;
}
