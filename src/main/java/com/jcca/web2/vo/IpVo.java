package com.jcca.web2.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description: ip信息
 * @author: sophia
 * @create: 2023/11/14 15:21
 **/
@Data
public class IpVo {
    private static final long serialVersionUID = 1L;

    private String id;
    /**
     * ip
     */
    private String ip;

    /**
     * 状态
     */
    private Byte status;

    /**
     * 审核状态
     */
    private Integer authStatus;

    /**
     * ping 占用状态
     */
    private Byte pingStatus;

    /**
     * ip的最后一位
     */
    private String ipNum;

    /**
     * ip的最后一位
     */
    private Integer ipNumSort;


    /**
     * ip上的设备ID
     */
    private String remark;

    /**
     * 掩码
     */
    private String mask;
    /**
     * 网关
     */
    private String gateway;

    /**
     * 上次检测(ping)时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date pingDate;
    /**
     * 设备名称
     * 如果IP分配的有设备
     * 则会填充此字段
     */
    private String assetName;
    /**
     * 设备设备ID
     * 如果IP分配的有设备
     * 则会填充此字段
     */
    private String assetId;

}