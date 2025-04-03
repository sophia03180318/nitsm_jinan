package com.jcca.web2.vo;

import lombok.Data;

/**
 * @description: 机柜内资产信息
 * @author: Lvyp
 * @create: 2023/10/24 10:39
 */
@Data
public class CabinetBaseAssetVo {
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 资产名称
     */
    private String name;
    /**
     * 设备型号
     */
    private Integer assetMode;
    /**
     * 开始U位
     */
    private Integer startPosition;
    /**
     * 结束U位
     */
    private Integer endPosition;
    /**
     * 告警数量
     */
    private String alarmSize;
    /**
     * 告警最高级别
     */
    private String alarmLevel;
    /**
     * 维护手册资料预览地址
     */
    private String viewUrl;

}
