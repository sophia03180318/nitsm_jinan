package com.jcca.web.common.service.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 动环属性
 * @author: sophia
 * @create: 2024/01/02 09:37
 **/
@Data
public class ThreeDPropertyReq implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 属性ID
     * */
    private String propertyId;

    /**
     * 设备ID
     */
    private String parentID;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String desc;

    /**
     * 单位
     */
    private String unit;

    /**
     * 数据值
     */
    private String value;

    /**
     * 数据状态
     */
    private int status;

}