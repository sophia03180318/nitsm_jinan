package com.jcca.web.asset.detail.bean;

import lombok.Data;

/**
 * @ClassName DetailGeneral
 * @Description 资产详情----基本信息
 * @Date 2020/5/11 14:04
 * @Author hanwone
 */
@Data
public class DetailGeneral {
    /**
     * 资产ID
     */
    private String id;
    /**
     * 资产名称
     */
    private String name;
    /**
     * 资产IP
     */
    private String ip;
    /**
     * 资产类型
     */
    private Integer assetMode;
    /**
     * 资产类型
     */
    private String assetModeStr;
    /**
     * 机房名称
     */
    private String roomName;
    /**
     * 机柜名称
     */
    private String cabinetName;
    /**
     * 制造厂商ID
     */
    private Integer manufacturerId;
    private String manufacturerStr;
    /**
     * 操作系统名称
     */
    private String operationSystem;
    /**
     * 操作系统版本
     */
    private String OSVersion;
    /**
     * 磁盘总容量
     */
    private String diskCapacity;
}
