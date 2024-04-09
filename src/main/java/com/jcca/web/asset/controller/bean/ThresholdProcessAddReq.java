package com.jcca.web.asset.controller.bean;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 新增
 *
 * @author Lvyp
 */
@Data
public class ThresholdProcessAddReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产ID
     */
    @NotEmpty(message = "资产ID不可空")
    private String assetId;
    /**
     * 进程名称
     */
    @NotEmpty(message = "进程名称不可空")
    private String processName;
    /**
     * 进程ID
     */
    @NotEmpty(message = "进程ID不可空")
    private String processId;
    /**
     * cpu阈值
     */
    @Pattern(regexp = "^(\\d|[1-9]\\d|100)(\\.\\d{1,2})?$", message = "cpu阈值配置不正确")
    @DecimalMin(value = "0.01", message = "cpu阈值最小值为0.01")
    private String thresholdCpu;
    /**
     * 内存阈值
     */
    @Pattern(regexp = "^(\\d|[1-9]\\d|100)(\\.\\d{1,2})?$", message = "内存阈值不正确")
    @DecimalMin(value = "0.01", message = "内存阈值最小值为0.01")
    private String thresholdMemory;

    private String remark;

}
