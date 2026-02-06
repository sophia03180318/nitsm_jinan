package com.jcca.component.thresholds.bean;

import com.jcca.dataProcessing.Entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 光交换机采集数据
 *
 * @version 1.2
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OpticalSwitchV2Bean extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 资产ID
     */
    private String assetId;

    /**
     * 采集时间
     */
    private Long collectTime;

    /**
     * 光端口状态，<索引,状态>
     */
    private Map<String, String> moduleStateMap;

    /**
     * 光端口类型，<索引,类型>
     */
    private Map<String, String> portTypeMap;

    /**
     * 光端口名称，<索引,名称>
     */
    private Map<String, String> portNameMap;

    /**
     * 最大带宽，<索引,带宽>
     */
    private Map<String, String> bandwidthMap;

    /**
     * 内存使用率
     */
    private CollectMemoryBean memoryBean;

    /**
     * 风扇状态，<索引,状态>
     */
    private List<TSensor> fanStateMap;

    /**
     * 传感器温度，<索引,温度>
     */
    private List<TSensor> temperatureMap;

    /**
     * 电源状态，<索引,状态>
     */
    private List<TSensor> pwrStateMap;

    /**
     * 健康状态
     */
    private List<TSensor> opticalHealthMap;

    /**
     * 光功率
     */
    private List<OpticalVo> opticalVoMap;

    /**
     * 2498-B24 日志收集
     */
    private List<String> logList;

    /**
     * 传感器温度，<索引,温度>
     */
    private Map<String, String> voltageMap;
}
