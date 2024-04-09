package com.jcca.web.common.service.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 3D机房：告警信息
 * @author: sophia
 * @create: 2023/12/04 15:45
 **/
@Data
public class ThreeDAlarmReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 告警ID
     * */
    private String id;

    /**
     * 告警标题
     */
    private String name;

    /**
     * 告警等级
     */
    private Integer level;

    /**
     * 设备ID
     */
    private String assetId;

    /**
     * 是否恢复
     */
    private Integer isRecover = 1;

    /**
     * 是否确认
     */
    private Integer isVerify = 1;

    /**
     * 是否显示告警
     */
    private Integer isShow = 1;

    /**
     * 备注信息 1
     */
    private String remark1;
    /**
     * 备注信息 2
     */
    private String remark2;
    /**
     * 备注信息 3
     */
    private String remark3;
}