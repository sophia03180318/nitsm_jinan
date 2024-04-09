package com.jcca.web.asset.service.bean;

import lombok.Data;

/**
 * 校验阈值是否告警
 *
 * @author lyp
 */
@Data
public class VerifyThresholdResp {

    /**
     * 是否告警
     */
    private Boolean alarmStatus;
    /**
     * 提示信息
     */
    private String msg;
    /**
     * 对比值
     */
    private String baseValue;

}
