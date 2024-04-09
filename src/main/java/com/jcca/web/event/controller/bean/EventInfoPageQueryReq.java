package com.jcca.web.event.controller.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 事件分页查询
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventInfoPageQueryReq extends PageQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 事件类型
     */
    private String eventTypeId;
    /**
     * 匹配码
     */
    private String uniqueCode;
    /**
     * 事件性质（异常事件、恢复事件、通知事件）
     */
    private String eventLevel;
    /**
     * 资产IP
     */
    private String assetIp;
    /**
     * 资产名称 syt
     */
    private String assetName;
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date beginTime;
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    /**
     * 时间范围
     * 单位天
     */
    private Integer timeScope;

    private String orgMsg;

}
