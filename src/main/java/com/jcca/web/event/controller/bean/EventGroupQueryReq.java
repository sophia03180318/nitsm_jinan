package com.jcca.web.event.controller.bean;

import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventGroupQueryReq extends PageQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 告警标题
     */
    private String name;
    /**
     * 告警类型
     */
    private Integer alarmType;
    /**
     * 告警级别
     */
    private Integer levle;
    /**
     * 是否可恢复
     */
    private Integer recoverFlag;

}
