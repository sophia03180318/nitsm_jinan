package com.jcca.web.ibmMQ.domain.vo;

import lombok.Data;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/30 13:11
 */
@Data
public class ReqMonitorVo {
    private String connectionId;
    private String connectionName;
    private String monitorName;
}
