package com.jcca.web.event.controller.bean;

import lombok.Data;

/**
 * 事件汇总响应
 *
 * @author lyp
 */
@Data
public class EventStatisticsResp {

    /**
     * 事件类型名称
     */
    private String typeName;
    /**
     * 事件类型总数
     */
    private Integer typeCount;
    /**
     * 总数
     */
    private Integer count;


}
