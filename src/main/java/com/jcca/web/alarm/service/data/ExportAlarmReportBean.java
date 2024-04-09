package com.jcca.web.alarm.service.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 导出表格需要的数据
 *
 * @author lyp
 */
@Data
public class ExportAlarmReportBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产IP
     */
    private String assetIp;
    /**
     * 组织名称
     */
    private String orgName;
    /**
     * 设备包含的所有明细
     */
    private List<ExportAlarmInfo> alarmInfos;
}
