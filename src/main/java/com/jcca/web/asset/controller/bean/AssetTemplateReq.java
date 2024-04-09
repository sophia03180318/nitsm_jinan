package com.jcca.web.asset.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 资产模板表
 *
 * @author syt
 * @date 2021-04-30 10:11:48
 **/
@Data
public class AssetTemplateReq {
    /**
     * 设备类型(183：主机，42：路由器，201：交换机，263：oracle数据库)
     */
    private Integer assetMode;

    /**
     * 生产厂商ID
     */
    @NotNull(message = "生产厂商ID不可为空")
    private Integer manufacturerId;

    /**
     * 设备图片(型号)
     */
    @NotEmpty(message = "设备型号不可为空")
    private String assetImage;

    /**
     * 20210112hanwone
     * 用于区分设备类型的细分，在原服务器183后面追加数字1，2，3
     * 1终端，2小型机，3工控机
     * 最终存为1831，1832，1833
     * 原设备类型不变存入该字段
     */
    @NotNull(message = "设备类型不可为空")
    private Integer desk;

}