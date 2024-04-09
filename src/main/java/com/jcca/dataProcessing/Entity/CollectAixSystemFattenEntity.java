package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * AIX系统采集
 *
 * @author lyp
 */
@Data
public class CollectAixSystemFattenEntity extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 系统版本
     */
    private String systemVersion;
    /**
     * cpu型号
     */
    private String cpuMode;
    /**
     * cpu个数
     */
    private Integer cpuNum;
    /**
     * 主频
     */
    private String frequency;
    /**
     * CPU核心数
     */
    private Integer cpuCoreNum;
    /**
     * 电源数量
     */
    private Integer powerNum;
    /**
     * 电源型号
     */
    private String powerModel;
    /**
     * 磁盘数量
     */
    private Integer diskNum;
    /**
     * 单磁盘容量
     */
    private Long diskCapacity;
    /**
     * 磁盘总容量
     * M
     */
    private Long diskCapacityCount;
    /**
     * 内存容量KB kbytes
     */
    private Long memory;
    /**
     * 序列号
     */
    private String serial;
    /**
     * IO卡采集
     */
    private String ioCard;
}
