package com.jcca.web2.vo;

import lombok.Data;

/**
 * @description: 告警信息
 * @author: sophia
 * @create: 2025/01/27 10:39
 */
@Data
public class WebAssetAlarmVo {

    /**
     * 资产列表
     */
    private String assetId;
    /**
     * 设备最大告警级别
     */
    private Integer alarmLevel;

}
