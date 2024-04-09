package com.jcca.web.asset.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName AssetExportVo
 * @Description 设备信息导出
 * @Date 2020/7/15 10:53
 * @Author hanwone
 */
@Data
public class AssetExportVo implements Serializable {
    /**
     * 设备名称
     */
    private String name;
    /**
     * 设备IP
     */
    private String ip;
    /**
     * 设备上架时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date onlineTime;
    /**
     * 设备管理口IP
     */
    private String ipmiIp;
    /**
     * 设备操作系统
     */
    private String operationSystem;
    /**
     * 设备编号
     */
    private String assetCode;
    /**
     * 设备运行状态
     */
    private String statusStr;
    /**
     * 设备类型
     */
    private String assetMode;
    /**
     * 设备生产厂商
     */
    private String manufacturerId;
    /**
     * 所属组织名称
     */
    private String orgId;
}
