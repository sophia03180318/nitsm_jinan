package com.jcca.web.ibmMQ.domain.vo;

import lombok.Data;

/**
 * @ Author：sophia
 * @ Date：Created in 10:31 2021/11/10
 * @ Description:
 */
@Data
public class WarningVo {
    private static final long serialVersionUID = 1L;
    private String id;
    private String currentQDepth;

}
