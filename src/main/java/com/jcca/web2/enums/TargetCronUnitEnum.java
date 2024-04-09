package com.jcca.web2.enums;

import com.jcca.web2.constant.Web2Const;

/**
 * @author HanHW
 * @description 采集周期表达式
 * @className TargetCronUnitEnum
 * @date 2024/2/29 17:13
 * @since 2.1.0.0
 */
public enum TargetCronUnitEnum {

    MONTH(11, "0 0 0 0 0/" + Web2Const.TARGET_INTERVAL + " ?"),
    DATE(30, "0 0 0 0/" + Web2Const.TARGET_INTERVAL + " * ?"),
    HOUR(23, "0 0 0/" + Web2Const.TARGET_INTERVAL + " * * ?"),
    MINUTE(59, "0 0/" + Web2Const.TARGET_INTERVAL + " * * * ?"),
    SECOND(59, "0/" + Web2Const.TARGET_INTERVAL + " * * * * ?"),

    ;


    public int interval;

    public String cron;

    TargetCronUnitEnum(Integer interval, String cron) {
        this.interval = interval;
        this.cron = cron;
    }

}
