package com.jcca.web.ip.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * IP 绑定的资产
 *
 * @author LYP
 */
@Data
public class IpBindingAssetVo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * ip
     */
    private String ip;
    /**
     * 资产code
     */
    private String assetCode;
    /**
     * 设备类型
     */
    private String assetModeStr;
    /**
     * 组织结构
     */
    private String orgName;
    /**
     * 机房名称
     */
    private String roomName;

    /**
     * 上架时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date onlineTime;
    /**
     * 下架时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date downlineTime;
    /**
     * 生产厂商名称
     */
    private Integer manufacturerName;
    /**
     * 机柜编号
     */
    private String cabinetCode;
    /**
     * 距离下架剩余毫秒
     */
    private Long millisecond;


}
