package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 阈值磁盘采集
 *
 * @author Lvyp
 */
@Data
public class CollectDiskEntity extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 磁盘名称
     */
    private String name;
    /**
     * 磁盘挂载点
     */
    private String mountPoint;
    /**
     * 磁盘总量
     * KB
     */
    private Long total;
    /**
     * 磁盘已使用
     * KB
     */
    private Long used;
    /**
     * 使用率
     * 新加的
     */
    private Double usedRate;
    /**
     * 磁盘可用
     */
    private Long available;




}
