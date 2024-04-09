package com.jcca.component.thresholds.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 阈值磁盘采集
 *
 * @author Lvyp
 */
@Data
public class CollectDiskBean implements Serializable {

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
     * 磁盘名称
     */
    @NotEmpty(message = "磁盘名称空")
    private String name;
    /**
     * 磁盘挂载点
     */
    private String mountPoint;
    /**
     * 磁盘总量
     * KB
     */
    @NotNull(message = "磁盘总量空")
    private Long total;
    /**
     * 磁盘已使用
     * KB
     */
    @NotNull(message = "磁盘已使用空")
    private Long used;
    /**
     * 磁盘可用
     */
    @NotNull(message = "磁盘可用空")
    private Long available;

}
