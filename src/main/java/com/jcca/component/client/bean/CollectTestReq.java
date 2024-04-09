package com.jcca.component.client.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 测试采集项是否正常
 *
 * @author Lvyp
 */
@Data
public class CollectTestReq implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 资产ID
     */
    @NotEmpty(message = "资产ID空")
    private String assetId;
    /**
     * 测试采集项目
     * 1：DB
     */
    @NotNull(message = "测试采集项目空")
    private Integer testType;
    /**
     * 内容
     */
    private ContentBean content;


}
