package com.jcca.web.common.controller.req;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 采集器推送过来的日志信息
 *
 * @author lyp
 */
@Data
public class CollectSyslogReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志的标识码
     */
    @NotEmpty(message = "缺少唯一标识")
    private String uniqueCode;

    @NotEmpty(message = "缺少设备ID")
    private String assetId;
    /**
     * 日志的原始信息
     */
    @NotEmpty(message = "缺少原始信息")
    private String orgMsg;
    /**
     * 日志的产生时间
     * yyyyMMddHHmmss
     */
    @NotEmpty(message = "缺少产生时间")
    private String createTime;
}
