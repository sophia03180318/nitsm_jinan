package com.jcca.web.statistics.service.bean;

import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.entity.CollectSensor;
import lombok.Data;

import java.util.List;

/**
 * 设备实时采集信息
 */
@Data
public class AssetMinuteMsg {

    /**
     * 温度实时采集结果
     */
    private List<CollectSensor> sensorRecords;
    /**
     * 内存实时采集结果
     */
    private List<CollectMemory> memoryRecords;
    /**
     * CPU实时采集结果
     */
    private List<CollectCpu> cpuRecords;

}
