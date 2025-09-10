package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhaozheng
 * @description TODO
 * @className CommonEntity
 * @date 2023/10/19 16:45
 * @since 2.1.0.0
 */
@Data
public class CommonEntity implements Serializable {
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 资产IP1
     */
    private String assetIp;
    /**
     * 资产IP2
     */
    private String assetIp2;
    /**
     * 资产名称
     */
    private String assetName;

    /**
     * 采集时间
     */
    private Long collectTime;
    /**
     * 资产类型
     */

    private String assetType;
    /**
     * 管理口或者设备IP
     */
    private String ip;

    /**
     * 存放性能信息
     */
    private Map<String, ChangeInfo> maps = new HashMap<>();
    /**
     * 采集批次码
     */
    private String collectCode;
    /**
     * 巡检采集任务ID
     */
    private String inspectRecordId;



}
