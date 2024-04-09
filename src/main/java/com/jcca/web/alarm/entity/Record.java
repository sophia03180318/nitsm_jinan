package com.jcca.web.alarm.entity;

import lombok.Data;

import java.util.List;

/**
 * @author zhaozheng@jccatech.com
 * @date 2021/3/16 20:48
 */
@Data
public class Record {
    private Integer index;
    private String level;//告警级别syt
    private String content;
    private List<Option> options;
    /**
     * 告警分类
     * AlarmTemplateEnum
     */
    private String alarmCategory;
}
