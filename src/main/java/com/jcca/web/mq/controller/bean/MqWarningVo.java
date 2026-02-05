package com.jcca.web.mq.controller.bean;

import lombok.Data;

/**
 * @ Author：sophia
 * @ Date：Created in 10:31 2021/11/10
 * @ Description:
 */
@Data
public class MqWarningVo {
    private static final long serialVersionUID = 1L;
    private String id;
    private Integer currentQDepth;

}
