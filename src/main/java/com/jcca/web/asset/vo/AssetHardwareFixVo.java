package com.jcca.web.asset.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @ClassName AssetHardwareFixVo
 * @Description 硬件更换记录
 * @Date 2020/7/17 15:38
 * @Author hanwone
 */
@Data
public class AssetHardwareFixVo {

    private String orgName;
    /**
     * 数据ID
     */
    private String id;
    /**
     * 资产ID
     */
    @NotEmpty(message = "请选择资产")
    private String assetId;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产IP
     */
    private String assetIp;
    /**
     * 资产类型
     */
    private String assetModeStr;
    private Integer assetMode;
    /**
     * 设备厂商
     */
    private String manufacturer;
    /**
     * 原FRU号码
     */
    private String fruBefore;
    /**
     * 现FRU号码
     */
    private String fruNow;
    /**
     * 中心签名
     */
    private String centerSignature;
    /**
     * 厂家签名
     */
    private String supplierSignature;
    /**
     * 硬件类别
     * AssetHardwareTypeEnum
     */
    private String hardwareTypeStr;
    @NotNull(message = "请选择硬件类别")
    private Integer hardwareType;
    /**
     * 更换时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fixTime;
    /**
     * 故障原因
     */
    private String reason;
    /**
     * 故障现象
     */
    private String description;
    /**
     * 插入时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
