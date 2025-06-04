package com.jcca.web2.dto.xunjian;

import lombok.Data;

import java.util.List;

/**
 * @author: hhw
 * @description: InspectReport1 主要是用来做为报告单1模板
 * @date: 2025-05-30  11:08
 * @since: 2.1.6.0
 */
@Data
public class InspectReport1 {

    // 序号
    private Integer index;
    // 巡检记录ID
    private String id;
    // 资产ID
    private String assetId;
    // 资产名称
    private String assetName;
    private String assetIp1;
    // 资产类型
    private Integer assetDesk;
    private String assetDeskStr;
    // 告警级别
    private Integer alarmLevel;
    // 告警标题
    private String alarmTitle;
    private String description;
    // 历史备注
    private List<String> remarks;
    private String remarkStr;

    private String alarmCode;
}
