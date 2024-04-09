package com.jcca.web2.vo;

import lombok.Data;

/**
 * @description: 监控指标项
 * @author: Lvyp
 * @create: 2023/11/17 10:23
 */
@Data
public class MonitoringItemVo {
    /**
     * 指标KEY
     */
    private String name;
    /**
     * code
     */
    private String code;
    /**
     * 指标TOTAL
     */
    private Integer total;

}
