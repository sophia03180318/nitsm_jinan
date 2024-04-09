package com.jcca.web.broken.controller.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 导出
 *
 * @author Lvyp
 */
@Data
public class BrokenRecordExportReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产ip
     */
    private String assetIp;
    /**
     * 故障现象
     */
    private String description;
    /**
     * 故障开始时间
     */
    private String occurStartTime;
    /**
     * 故障结束时间
     */
    private String occurEndTime;
    /**
     * 状态 BrokenRecordConst
     */
    private Byte status;

}
