package com.jcca.web.asset.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName AssetHardwareFixVo
 * @Description 硬件更换记录导出
 * @Date 2020/9/7 15:38
 * @Author hanwone
 */
@Data
public class AssetHardwareFixExportVo {
    /**
     * 资产名称
     */
    private String assetName;

    private String assetId;
    /**
     * 资产IP
     */
    private String assetIp;
    /**
     * 资产类型
     */
    private String assetModeStr;
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
     * 硬件类别
     * AssetHardwareTypeEnum
     */
    private String hardwareTypeStr;
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

    private String orgName;
}
