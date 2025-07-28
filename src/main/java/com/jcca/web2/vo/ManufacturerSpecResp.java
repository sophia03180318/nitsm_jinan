package com.jcca.web2.vo;


import com.jcca.admin.system.entity.PerformanceTarget;
import lombok.Data;

import java.util.List;

/**
 * 厂商对应的执行列表
 */
@Data
public class ManufacturerSpecResp {


    /**
     * 指标列表
     */
    private List<PerformanceTarget> targetList;
    /**
     * 采集配置标题
     */
    private String specDictTitle;
    /**
     * 采集类型
     */
    private Integer systemType;

}
