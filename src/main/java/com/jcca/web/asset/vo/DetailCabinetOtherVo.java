package com.jcca.web.asset.vo;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName DetailCabinetOtherVo
 * @Description 机柜内设备其他数据
 * @Date 2020/7/1 14:46
 * @Author hanwone
 */
@Data
public class DetailCabinetOtherVo {
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * IP
     */
    private String ip;
    /**
     * 资产型号
     */
    private Integer assetMode;
    /**
     * 资产类型
     */
    private String assetImage;
    /**
     * 设备名称
     */
    private String assetName;
    /**
     * 服务器连接数
     */
    private Integer connectionNum;
    /**
     * 服务器系统时间
     */
    private String systemTime;
    /**
     * cpu使用率
     */
    private Double cpuUsedRate;
    /**
     * 内存使用率
     */
    private Double memUsedRate;
    /**
     * 磁盘使用率
     */
    private Double usedRate;
    /**
     * 时间偏差值,远程设备和当前设备比，慢了为负，快了为正
     */
    private Long timeOffset;
    /**
     * 设备运行时长
     */
    private Long timeduration;
    /**
     * 温度
     */
    private String temperature;

    /**
     * 存储总容量
     */
    private String totalCapacity;

    /**
     * 存储使用量
     */
    private String usedCapacity;

    /**
     * 存储剩余容量
     */
    private String freeCapacity;

}
