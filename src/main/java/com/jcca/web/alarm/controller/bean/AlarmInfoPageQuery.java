package com.jcca.web.alarm.controller.bean;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 告警信息查询
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AlarmInfoPageQuery extends PageQuery {

    private static final long serialVersionUID = 1L;

    /**
     * yes/no
     */
    private String showJcca;
    /**
     * 组织ID
     */
    private String orgId;
    /**
     * 设备ip
     */
    private String ip;
    /**
     * 资产IPv2
     */
    private String assetIp;
    /**
     * 告警开始时间V2
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    /**
     * 告警结束时间V2
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    /**
     * alarmCode V2
     */
    private String eventCategory;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 处理状况/告警状态
     */
    private Byte status;
    /**
     * 告警标题
     */
    private String title;
    /**
     * 告警级别
     */
    private Byte alarmLevel;
    /**
     * 告警类型
     */
    private Byte type;
    /**
     * 是否已恢复
     */
    private Byte alarmState;

    /**
     * 是否是天窗告警
     */
    private Byte blank;

    /**
     * 告警开始时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startDate;
    /**
     * 告警结束时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;

    /**
     * 组织ID列表
     */
    private List<String> orgIds;

    /**
     * 告警ID
     */
    private List<String> idList;

    /**
     * 设备类型(183：主机，42：路由器，201：交换机，263：oracle数据库)
     */
    private Integer assetMode;

    /**
     * 备注
     */
    private String remark;

    /**
     * 排序字段
     */
    private String sort;

    /**
     * 排序方式
     */
    private String order;

    /**
     * 0未知告警
     * 1一级告警
     * 2二级告警
     * 3三级告警
     * 4信息通知
     */
    private List<Integer> alarmLevelList;
    /**
     * 告警项的code
     */
    private List<String> alarmCodeList;

    /**
     * 资产小型号筛选集合
     */
    private List<Integer> assetDeskList;

}
