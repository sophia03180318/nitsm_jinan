package com.jcca.component.dto;

import com.jcca.dataProcessing.Entity.CommonEntity;
import lombok.Data;

/**
 * @ClassName ReceiveAlarmDto
 * @Description 接收采集器推送告警实体类
 * @Date 2020/6/5 13:23
 * @Author hanwone
 */
@Data
public class ReceiveAlarmDto extends CommonEntity {

    /**
     * 原始告警内容
     */
    private String content;
    /**
     * 推送告警时间 yyyy-MM-dd HH:mm:ss
     */
    private String occurTime;
    /**
     * 告警种类
     * ReceiveAlarmTypeEnum
     */
    private String category;
    /**
     * 有/通/恢复-true, 无/断/告警-false
     */
    private Boolean flag;

    /**
     * 告警级别有的填没有不填
     */
    private Integer alarmLevel;

    /**
     * 拼接的关键字
     */
    private String jccaSyslogLevel;

}
