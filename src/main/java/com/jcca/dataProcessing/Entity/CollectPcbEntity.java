package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 板卡表
 *
 * @author Lvyp
 */
@Data
public class CollectPcbEntity extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 板卡名称
     */
    private String name;
    /**
     * 板卡描述
     */
    private String descStr;
    /**
     * 模式名称
     */
    private String modelName;
    /**
     * 板卡类型
     */
    private String type;
    /**
     * 物理索引
     */
    private String entPhysicalIndex;
    /**
     * 直接父级物理索引物理索引 0表示顶级
     */
    private String entPhysicalContainedIn;
    /**
     * 在父节点中的相对索引（排序编号） 子排序
     */
    private String entPhysicalParRelPos;
    /**
     * 此模块是否支持插拔
     */
    private String entPhysicalIsFRU;
    /**
     * 板卡状态采集
     * 1: unknown (未知)
     * 2: up (正常工作)
     * 3: disabled (禁用)
     * 4: okButDiagFailed (诊断失败但基本功能正常)
     */
    private String entPhysicalCardStatusRev;
    /**
     * 序列号
     */
    private String serialNumber;
    /**
     * 序列号名称
     */
    private String serialNumberName;
    /**
     * 软件版本
     */
    private String softwareVersion;
    /**
     * 硬件版本
     */
    private String hardwareVersion;
    /**
     * 系统版本
     */
    private String osVersion;
    /**
     * 板卡索引号
     */
    private String pcbIndex;

}
