package com.jcca.component.other.bean;

import lombok.Data;

/**
 * @ClassName SyslogContentBean
 * @Description syslog告警内容实体
 * @Date 2020/9/4 10:24
 * @Author hanwone
 */
@Data
public class SyslogContentBean {
    private String charSet;
    private Byte level;
    private String host;
    private String rawLength;
    private Boolean isHostStrippedFromMessage;
    private String message;
    private Integer facility;
}
