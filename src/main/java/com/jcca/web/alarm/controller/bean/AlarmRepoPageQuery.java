package com.jcca.web.alarm.controller.bean;

import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页查询告警记录
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AlarmRepoPageQuery extends PageQuery {

    private static final long serialVersionUID = 1L;
    /**
     * 名称
     */
    private String name;
    /**
     * 告警级别
     */
    private Byte alarmLevel;
    /**
     * 告警编号
     */
    private String alarmCode;
    /**
     * 厂商ID
     */
    private Integer manufacturerId;

}
