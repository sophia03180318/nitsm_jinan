package com.jcca.web.alarm.controller.bean;

import com.jcca.web.alarm.service.data.ExportAlarmReportBean;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 缓存结果
 *
 * @author lyp
 */
@Data
public class ExportAlarmReportCacheBean implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 查询结果
     */
    private Map<String, List<ExportAlarmReportBean>> excelDataResult;
    /**
     * 不生效时间配置
     */
    private List<List<String>> timeConfig;
    /**
     * 表生成方式
     * one\more
     */
    private String tabType;

}
