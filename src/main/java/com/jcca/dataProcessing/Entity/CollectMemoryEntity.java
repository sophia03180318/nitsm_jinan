package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 运行内存采集数据
 *
 * @author Lvyp
 */
@Data
public class CollectMemoryEntity extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 内存总量 b
     */
    private Long memTotal;
    /**
     * 内存已使用 b
     */
    private Long memUsed;
    /**
     * 置换总量 b
     */
    private Long swapTotal;
    /**
     * 置换已使用 b
     */
    private Long swapUsed;
    /**
     * 内存使用率
     */
    private Double memRate;

    /**
     * 进程内存占用率较大前5
     */
    private List<CollectProcessEntity> processTop5List;

}
