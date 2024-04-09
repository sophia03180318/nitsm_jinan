package com.jcca.web.statistics.service.bean;

import com.jcca.web.statistics.entity.HourCpu;
import com.jcca.web.statistics.entity.HourMemory;
import com.jcca.web.statistics.entity.HourTemp;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 资产一小时CPU、内存、温度
 */
@Data
public class AssetHourMsg implements Serializable {

    private String assetName;

    private String assetId;

    private List<HourCpu> cpuUsedRateList;

    private List<HourMemory> memoryRateList;

    private List<HourTemp> temperatureList;
}
