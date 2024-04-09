package com.jcca.component.thresholds.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 连接数
 */
@Data
public class CollectConnectBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 采集时间
     */
    @NotEmpty(message = "采集时间不能空")
    private String collectTime;
    /**
     * 资产ID
     */
    @NotEmpty(message = "资产ID不能空")
    private String assetId;

    /**
     * 连接数
     */
    @NotEmpty(message = "连接数不能为空")
    private Integer establishedNum;
}
