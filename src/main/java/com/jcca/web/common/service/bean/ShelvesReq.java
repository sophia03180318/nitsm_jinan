package com.jcca.web.common.service.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 3D机房：设备信息
 * @author: sophia
 * @create: 2023/12/04 15:45
 **/
@Data
public class ShelvesReq implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 名称
     */
    private String name;

    /**
     * IP
     */
    private String ip;

    /**
     * IP2
     */
    private String ip2;


    /**
     * 资产型号
     */
    private String categoryId="2_2";

    /**
     * 机房ID
     * */
    private String areaId;

    /**
     * 三维模型ID
     * */
    private String threeModel="225";

    /**
     * 机柜ID
     */
    private String cabinetId;

    /**
     * 机柜名称
     */
    private String cabinetName;

    /**
     * 起始U位
     */
    private String startU;

    /**
     * 是否展示三维模型
     */
    private Integer isShowThree = 1;

    /**
     * 机柜正反  1=正面
     */
    private Integer frontBack = 1;

    /**
     * 是否上架，1是
     */
    private Integer isShelf = 1;

    /**
     * 购买时间 2023-10-09
     * */
    private String purchaseTime="2023-10-09";

    /**
     * 备注信息 1
     */
    private String remark1;
    /**
     * 备注信息 2
     */
    private String remark2;
    /**
     * 备注信息 3
     */
    private String remark3;
}