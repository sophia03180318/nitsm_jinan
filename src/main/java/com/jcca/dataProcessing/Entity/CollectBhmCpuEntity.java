package com.jcca.dataProcessing.Entity;


import lombok.Data;

import java.io.Serializable;

/**
 * 采集管理口的CPU信息
 */
@Data
public class CollectBhmCpuEntity extends CommonEntity implements Serializable {

    /**
     * CPU 在服务器内的本地标识 ID，通常与物理插槽编号对应。
     */
    private String id;
    /**
     * CPU 的易读名称，用于可视化管理界面（如服务器 BMC 界面）显示，直观区分多颗 CPU。
     */
    private String name;
    /**
     * CPU 制造商
     */
    private String manufacturer;
    /**
     * CPU 具体型号
     */
    private String model;
    /**
     *支持的指令集架构
     */
    private String instructionSet;
    /**
     * 处理器架构
     */
    private String processorArchitecture;
    /**
     * 处理器类型
     */
    private String processorType;
    /**
     * CPU 对应的物理插槽编号
     */
    private String socket;
    /**
     * CPU 物理核心总数
     */
    private Integer totalCores;
    /**
     * CPU 逻辑线程总数
     */
    private Integer totalThreads;
    /**
     * CPU 单核最高睿频速度
     */
    private Integer maxSpeedMHz;

    private ReadFishStatusEntity status;


}
