package com.jcca.web2.dto.xunjian;


import lombok.Data;

/**
 * @author lifp
 * @version 1.0
 * @description: 智能巡检-柱状图基础数据
 * @date 2025-10-09 星期四 10:50:41
 */
@Data
public class InspectEcharts {
    // 指标Id
    String eventTypeId;

    // 指标名称
    String eventTypeName;

    // 告警总条数 默认 0
    int alarmCount;

    // 资产总条数 默认 0
    int assetCount;
}
