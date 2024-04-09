package com.jcca.dataProcessing.Entity;

import com.jcca.component.thresholds.bean.CollectMemoryBean;
import com.jcca.component.thresholds.bean.OpticalVo;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Description 光交换机采集数据
 * @ClassName OpticalSwitchBean
 * @Date 2022/6/8 17:27
 * @Author hanwone
 * @Since 2.0.0.1
 */
@Data
public class OpticalSwitchEntity extends CommonEntity {



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
    private Map<String, String> fanStateMap;

    /**
     * 传感器温度，<索引,温度>
     */
    private Map<String, String> temperatureMap;

    /**
     * 电源状态，<索引,状态>
     */
    private Map<String, String> pwrStateMap;

    /**
     * 健康状态
     */
    private Map<String, String> opticalHealthMap;

    /**
     * 光功率
     */
    private Map<String, OpticalVo> opticalVoMap;

    /**
     * 2498-B24 日志收集
     */
    private List<String> logList;
}
