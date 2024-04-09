package com.jcca.web.ibmMQ.domain;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/29 9:40
 */
@Data
public class ListMonitorsReq {

    private Integer page;

    private Integer size;

    /**
     * 监控连接ID
     */
    @NotEmpty(message = "设备队列ID不能为空")
    private String connectionId;
    /**
     * 监控器名称
     */
    private String name;

}
