package com.jcca.component.thresholds.bean;

import java.io.Serializable;

/**
 * AIX系统采集
 *
 * @author lyp
 */
public class CollectAixSystemFattenBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String assetId;
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

    public String getIoCard() {
        return ioCard;
    }

    public void setIoCard(String ioCard) {
        this.ioCard = ioCard;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getCpuMode() {
        return cpuMode;
    }

    public void setCpuMode(String cpuMode) {
        this.cpuMode = cpuMode;
    }

    public Integer getCpuNum() {
        return cpuNum;
    }

    public void setCpuNum(Integer cpuNum) {
        this.cpuNum = cpuNum;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getCpuCoreNum() {
        return cpuCoreNum;
    }

    public void setCpuCoreNum(Integer cpuCoreNum) {
        this.cpuCoreNum = cpuCoreNum;
    }

    public Integer getPowerNum() {
        return powerNum;
    }

    public void setPowerNum(Integer powerNum) {
        this.powerNum = powerNum;
    }

    public String getPowerModel() {
        return powerModel;
    }

    public void setPowerModel(String powerModel) {
        this.powerModel = powerModel;
    }

    public Integer getDiskNum() {
        return diskNum;
    }

    public void setDiskNum(Integer diskNum) {
        this.diskNum = diskNum;
    }

    public Long getDiskCapacity() {
        return diskCapacity;
    }

    public void setDiskCapacity(Long diskCapacity) {
        this.diskCapacity = diskCapacity;
    }

    public Long getMemory() {
        return memory;
    }

    public void setMemory(Long memory) {
        this.memory = memory;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public Long getDiskCapacityCount() {
        return diskCapacityCount;
    }

    public void setDiskCapacityCount(Long diskCapacityCount) {
        this.diskCapacityCount = diskCapacityCount;
    }

}
