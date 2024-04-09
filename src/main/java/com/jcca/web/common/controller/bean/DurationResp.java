package com.jcca.web.common.controller.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 运行时长同步数据
 *
 * @author lyp
 */
@Data
public class DurationResp implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 运行时长天
     */
    private Long duration;
    /**
     * 设备型号
     */
    private Integer assetMode;
    /**
     * 资产IP
     */
    private String assetIp;
}
