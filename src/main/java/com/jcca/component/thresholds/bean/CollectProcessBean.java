package com.jcca.component.thresholds.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 阈值-进程采集
 *
 * @author Lvyp
 */
@Data
public class CollectProcessBean implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 资产ID
     */
    @NotEmpty(message = "资产id空")
    private String assetId;
    /**
     * 采集时间
     */
    @NotEmpty(message = "采集时间空")
    private String collectTime;
    /**
     * 进程名称
     */
    @NotEmpty(message = "进程名称空")
    private String name;
    /**
     * 进程ID
     */
    @NotEmpty(message = "进程ID")
    private String processId;
    /**
     * cpu使用率
     */
    @NotEmpty(message = "cpu使用率空")
    @Pattern(regexp = "^(\\d|[1-9]\\d|100)(\\.\\d{1,2})?$", message = "cpu使用率不正确")
    private String cpuRate;
    /**
     * 内存使用率
     */
    @NotEmpty(message = "内存使用率空")
    @Pattern(regexp = "^(\\d|[1-9]\\d|100)(\\.\\d{1,2})?$", message = "内存使用率不正确")
    private String memoryRate;

}
