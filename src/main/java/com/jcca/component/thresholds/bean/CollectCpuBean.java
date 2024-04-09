package com.jcca.component.thresholds.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;

/**
 * cpu数据采集
 *
 * @author Lvyp
 */
@Data
public class CollectCpuBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产主键
     */
    @NotEmpty(message = "资产主键空")
    private String assetId;
    /**
     * 采集时间
     */
    @NotEmpty(message = "采集时间空")
    private String collectTime;
    /**
     * cpu使用率
     */
    @NotEmpty(message = "cpu使用率空")
    @Pattern(regexp = "^(\\d|[1-9]\\d|100)(\\.\\d{1,2})?$", message = "cpu使用率不正确")
    private String cpuUsedRate;
    /**
     * cpu标识 一台设备多个cpu的时候 此参数要唯一
     */
    @NotEmpty(message = "cpuFlg 空")
    private String cpuFlg;

    /**
     * 进程CPU占用率较大前5
     */
    private List<CollectProcessBean> processTop5List;
}
