package com.jcca.web.common.service.bean;


import lombok.Data;

/**
 * 告警文件
 *
 * @author sophia
 */
@Data
public class PullAlertLogResultResp {

    public static final String SUNNCESS = "0";
    public static final String ERROR = "-1";
    public static final String WARRING = "-2";

    /**
     * 响应码
     */
    private String code;
    /**
     * 响应消息
     */
    private String msg;
    /**
     * 相应结果
     */
    private String resultList;

}
