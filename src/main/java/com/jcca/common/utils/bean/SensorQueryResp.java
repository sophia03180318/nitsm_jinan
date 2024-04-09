package com.jcca.common.utils.bean;

import lombok.Data;

import java.util.List;

/**
 * 传感信息查询
 *
 * @author Lvyp
 */
@Data
public class SensorQueryResp {

    /**
     * 资产主键
     */
    private String assetId;
    /**
     * 采集时间 时间戳
     */
    private String collectDate;
    /**
     * 采集编号
     */
    private String collectCode;
    /**
     * 风扇
     */
    private List<SensorItsm> fanList;
    /**
     * 温度
     */
    private List<SensorItsm> temList;
    /**
     * 电源状态
     */
    private List<SensorItsm> sysBrdList;
    /**
     * 板卡电压状态
     */
    private List<SensorItsm> sysPlanarList;

    /**
     * log
     */
    private List<SensorItsm> logList;

    /**
     * 厂商
     */
    private List<SensorItsm> manuList;

    /**
     * Led灯
     */
    private List<SensorItsm> ledList;

    /**
     * CPU
     */
    private List<CpuIpmi> cpuList;
}
