package com.jcca.web.alarm.controller.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 导出告警分析报告
 *
 * @author lyp
 */
@Data
public class ExportAlarmReportReq implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 导出类型
     * type\org
     */
    private String exportType;
    /**
     * 表生成方式
     * one\more
     */
    private String tabType;
    /**
     * 如果是按照类型导出则需要此项
     * OrgTypeEnum
     */
    private List<Byte> typeDetail;
    /**
     * 如果是按照组织导出则需传入此项
     * 组织ID集合
     */
    private List<String> orgDetail;
    /**
     * 排除告警的时间配置
     * 导出的分析报告
     * 将不包括配置时间段里面的所有记录
     */
    private List<List<String>> timeConfig;
    /**
     * 生成的开始时间范围
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startDate;
    /**
     * 生成的结束时间范围
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDate;

    /**
     * 设备类型(183：主机，42：路由器，201：交换机，263：oracle数据库)
     */
    private Integer assetMode;
}

