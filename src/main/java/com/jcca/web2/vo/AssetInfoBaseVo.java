package com.jcca.web2.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description: 资产详情界面基础信息
 * @author: Lvyp
 * @create: 2023/11/20 14:09
 */
@Data
public class AssetInfoBaseVo {

    /**
     * 设备ID
     */
    private String id;
    /**
     * 设备名称
     */
    private String name;
    /**
     * 设备状态(0：离线，1：在线，2：不监控,4:未知)
     */
    private Byte status;
    /**
     * 是否监控
     * 0不监控 1监控
     */
    private Byte watch;
    /**
     * 资产编号
     */
    private String assetCode;
    /**
     * 资产型号
     */
    private String assetMode;
    /**
     * 机房名称
     */
    private String roomName;
    /**
     * 机柜名称
     */
    private String cabinetName;
    /**
     * 厂家code
     */
    private Integer manufacturerCode;
    /**
     * 厂家name
     */
    private String manufacturerName;
    /**
     * 系统
     */
    private String operationSystem;
    /**
     * 版本
     */
    private String assetVersion;
    /**
     * 型号
     */
    private String assetImage;
    /**
     * 型号图片
     */
    private String assetImagePath;
    /**
     * ip1
     */
    private String ip1;
    /**
     * ip2
     */
    private String ip2;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;

}
