package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 连接数
 *
 * @author lyp
 */
@Data
public class CollectConnectEntity extends CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 连接数
     */
    private Integer establishedNum;

    /**
     * 阈值相关数据
     */
    private ThresholdBaseEntity threshold;

}
