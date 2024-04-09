package com.jcca.web2.vo;

import lombok.Data;

/**
 * @author HanHW
 * @description 维护计划统计
 * @className MaintenanceStatisticsVo
 * @date 2023/12/14 17:01
 * @since 2.1.0.0
 */
@Data
public class MaintenanceStatisticsVo {

    /**
     * 施工类型
     */
    private String type;

    /**
     * 施工月份
     */
    private Integer month;

    /**
     * 类型数量
     */
    private Integer count;
}
