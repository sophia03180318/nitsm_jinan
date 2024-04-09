package com.jcca.web.asset.controller.bean;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 资产表
 *
 * @author hanwone
 * @date 2020-04-20 15:37:48
 **/
@Data
public class AssetCollectReq implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    private String id;
    /**
     * 资产别名
     */
    @NotEmpty(message = "请输入资产名称")
    @Length(max = 64, message = "设备名字不能超过60个字符")
    private String name;
    /**
     * 资产编号
     */
    @NotEmpty(message = "请输入资产编号")
    private String assetCode;
    /**
     * 20210112hanwone
     * 用于区分设备类型的细分，在原服务器183后面追加数字1，2，3
     * 1终端，2小型机，3工控机
     * 最终存为1831，1832，1833
     * 原设备类型不变存入该字段
     */
    private Integer desk;
    /**
     * Ip地址
     */
    //@Pattern(message = "ip地址格式不正确", regexp = "^((25[0-5]|2[0-4]\\d|[1]\\d\\d|[1-9]\\d|\\d)($|(?!\\.$)\\.)){4}$")
    @NotEmpty(message = "请输入IP")
    private String ip;


    private String ip2;
    /**
     * 设备登录账号
     */
    private String osUser;
    /**
     * 设备登录密码
     */
    private String osPassword;
    /**
     * 设备类型(183：主机，42：路由器，201：交换机，263：oracle数据库)
     */
    @NotNull(message = "请选择资产类型")
    private Integer assetMode;
    /**
     * 生产厂商ID
     */
    @NotNull(message = "请输入资产厂商")
    private Integer manufacturerId;
    /**
     * 组织ID
     */
    @NotEmpty(message = "请选择资产所属组织")
    private String orgId;
    /**
     * 采集操作系统类型
     */
    private Integer collectionType;
    /**
     * 设备图片(资产型号)
     */
    private String assetImage;
    /**
     * 设备是否监控
     * 0-不监控，1-监控
     */
    private Byte watch;

    /**
     * 机房ID
     */
    private String roomId;
    /**
     * 机柜ID
     */
    private String cabinetId;

    private Integer startPosition;
    private Integer endPosition;
    private Integer port;

}