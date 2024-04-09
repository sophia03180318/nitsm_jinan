package com.jcca.web.asset.enums;

import com.jcca.web.asset.vo.ThresholdAssetVo;

import java.util.Objects;

/**
 * 区间阈值类型
 *
 * @author lyp
 */
public enum ThresholdSectionEnum {

    /**
     * CPU
     */
    CPU("CPU使用率采集到阈值"),
    /**
     * 内存
     */
    MENORY("内存使用率采集到阈值"),
    /**
     * 端口流入
     */
    PORT_IN("端口流入率采集到阈值"),
    /**
     * 端口流出
     */
    PORT_OUT("端口流出率采集到阈值"),
    /**
     * 发送丢包
     */
    SEND_LOSE("发送丢包率采集到阈值"),
    /**
     * 接收丢包
     */
    RECEIVE_LOSE("接收丢包率采集到阈值"),
    /**
     * 发送误码率
     */
    SEND_ERROR_CODE("发送误码率采集到阈值"),
    /**
     * 接收误码率
     */
    RECEIVE_ERROR_CODE("接收误码率采集到阈值"),
    /**
     * 发送功率
     */
    SEND_POWER("发送光功率采集到阈值"),
    /**
     * 接收功率
     */
    RECEIVE_POWER("接收光功率采集到阈值"),

    /**
     * 磁盘使用率
     */
    DSIK("磁盘使用率采集到阈值"),
    /**
     * 表空间
     */
    DB_SPACE_NAME("表空间使用率采集到阈值");

    private String msg;

    public String getMsg() {
        return msg;
    }

    private ThresholdSectionEnum(String msg) {
        this.msg = msg;
    }

    public static String getBaseValue(ThresholdAssetVo threshold, ThresholdSectionEnum type) {
        if (Objects.isNull(threshold)) {
            threshold = new ThresholdAssetVo();
        }

        if (CPU == type) {
            if (Objects.isNull(threshold.getCpu())) {
                threshold.setCpu(100d);
            }
            return threshold.getCpu().toString();
        }
        if (MENORY == type) {
            if (Objects.isNull(threshold.getMemory())) {
                threshold.setMemory(100d);
            }
            return threshold.getMemory().toString();
        }
        if (PORT_IN == type) {
            if (Objects.isNull(threshold.getPortRateIn())) {
                threshold.setPortRateIn(100d);
            }
            return threshold.getPortRateIn().toString();
        }
        if (PORT_OUT == type) {
            if (Objects.isNull(threshold.getPortRateOut())) {
                threshold.setPortRateOut(100d);
            }
            return threshold.getPortRateOut().toString();
        }
        if (SEND_LOSE == type) {
            if (Objects.isNull(threshold.getPacketLossOut())) {
                threshold.setPacketLossOut(100d);
            }
            return threshold.getPacketLossOut().toString();
        }
        if (RECEIVE_LOSE == type) {
            if (Objects.isNull(threshold.getPacketLossIn())) {
                threshold.setPacketLossIn(100d);
            }
            return threshold.getPacketLossIn().toString();
        }
        if (SEND_ERROR_CODE == type) {
            if (Objects.isNull(threshold.getCodeErrorOut())) {
                threshold.setCodeErrorOut(100d);
            }
            return threshold.getCodeErrorOut().toString();
        }
        if (DSIK == type) {
            if (Objects.isNull(threshold.getDisk())) {
                threshold.setDisk(100d);
            }
            return threshold.getDisk().toString();
        }
        if (DB_SPACE_NAME == type) {
            if (Objects.isNull(threshold.getTablespace())) {
                threshold.setTablespace(100d);
            }
            return threshold.getTablespace().toString();
        }

        return null;
    }

}
