package com.jcca.web2.dto;

import lombok.Data;

/**
 * @description: 告警规则分页查询
 * @author: Lvyp
 * @create: 2023/11/29 17:09
 */
@Data
public class EventRpoPageDto extends PageDto {

    /**
     * 名称
     */
    private String name;
    /**
     * 事件类型级别
     */
    private Integer alarmLevel;
    /**
     * 匹配码
     */
    private String alarmCode;
    /***
     * 正常异常
     */
    private Integer flagType;
    /**
     * 事件类型ID
     */
    private String eventTypeId;

}
