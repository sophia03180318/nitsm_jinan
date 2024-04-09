package com.jcca.web.common.service.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 3D机房：链路信息
 * @author: sophia
 * @create: 2023/12/04 15:45
 **/
@Data
public class ThreeDLinkReq implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 设备ID
     */
    private String assetAId;

    /**
     * 设备端口
     */
    private String portAId;

    /**
     * 设备ID
     */
    private String assetBId;

    /**
     * 设备端口
     */
    private String portBId;

    /**
     * 显示颜色 绿=83d35c 红=F5222D
     */
    private String color = "#83d35c";

    /**
     * 是否显示告警  0：正常（绿色），1：异常（红色）
     */
    private Integer lineState = 0;

}