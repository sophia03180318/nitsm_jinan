package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.util.List;

/**
 * 传感信息查询
 *
 * @author Lvyp
 */
@Data
public class SensorEntity extends CommonEntity {


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
    private List<CollectSensorEntity> fanList;
    /**
     * 温度
     */
    private List<CollectSensorEntity> temList;
    /**
     * 电源状态
     */
    private List<CollectSensorEntity> sysBrdList;
    /**
     * 板卡电压状态
     */
    private List<CollectSensorEntity> sysPlanarList;

    /**
     * log
     */
    private List<CollectSensorEntity> logList;

    /**
     * 厂商
     */
    private List<CollectSensorEntity> manuList;

    /**
     * Led灯
     */
    private List<CollectSensorEntity> ledList;

    /**
     * CPU
     */
    private List<CollectSensorEntity> cpuList;
}
