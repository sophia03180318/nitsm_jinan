package com.jcca.component.thresholds.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 板卡表
 *
 * @author Lvyp
 */
@Data
public class CollectPcbBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 采集时间
     */
    @NotEmpty(message = "采集时间不能空")
    private String collectTime;
    /**
     * 资产ID
     */
    @NotEmpty(message = "资产ID不能空")
    private String assetId;
    /**
     * 板卡名称
     */
    @NotEmpty(message = "板卡名称不能空")
    private String name;
    /**
     * 板卡描述
     */
    @NotEmpty(message = "板卡描述不能空")
    private String descStr;
    /**
     * 模式名称
     */
    @NotEmpty(message = "模式名称不能空")
    private String modelName;
    /**
     * 板卡类型
     */
    @NotEmpty(message = "板卡类型不能空")
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
     * 序列号
     */
    @NotEmpty(message = "序列号不能空")
    private String serialNumber;
    /**
     * 序列号名称
     */
    @NotEmpty(message = "序列号名称不能空")
    private String serialNumberName;
    /**
     * 软件版本
     */
    @NotEmpty(message = "软件版本不能空")
    private String softwareVersion;
    /**
     * 硬件版本
     */
    @NotEmpty(message = "硬件版本不可空")
    private String hardwareVersion;
    /**
     * 系统版本
     */
    @NotEmpty(message = "系统版本不能空")
    private String osVersion;
    /**
     * 板卡索引号
     */
    @NotEmpty(message = "板卡索引号不能空")
    private String pcbIndex;

}
