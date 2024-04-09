package com.jcca.web.common.controller.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author HanHW
 * @description 车站同步ITSM进程实体
 * @className CollectProcess
 * @date 2023/8/30 11:25
 * @since 2.0.6.0
 */
@Data
@EqualsAndHashCode
public class CollectProcess implements Serializable {
    private static final long serialVersionUID = 2497084650788668249L;

    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 进程名称
     */
    private String processName;
    /**
     * 当前状态
     * normal、abnormal
     */
    private String status;
}
