package com.jcca.component.thresholds.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 运行内存采集数据
 *
 * @author Lvyp
 */
@Data
public class CollectMemoryBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产主键
     */
    @NotEmpty(message = "资产主键空")
    private String assetId;
    /**
     * 采集时间
     */
    @NotNull(message = "采集时间空")
    private String collectTime;
    /**
     * 内存总量 b
     */
    @NotNull(message = "mem总量空")
    private Long memTotal;
    /**
     * 内存已使用 b
     */
    @NotNull(message = "mem已使用空")
    private Long memUsed;
    /**
     * 置换总量 b
     */
    @NotNull(message = "swap总量空")
    private Long swapTotal;
    /**
     * 置换已使用 b
     */
    @NotNull(message = "swap已使用空")
    private Long swapUsed;

    /**
     * 进程内存占用率较大前5
     */
    private List<CollectProcessBean> processTop5List;
}
