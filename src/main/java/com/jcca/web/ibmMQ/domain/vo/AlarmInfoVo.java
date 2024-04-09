package com.jcca.web.ibmMQ.domain.vo;

import lombok.Data;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/31 16:54
 */
@Data
public class AlarmInfoVo {
    private Integer level;
    private String content;
    private String code;
}
