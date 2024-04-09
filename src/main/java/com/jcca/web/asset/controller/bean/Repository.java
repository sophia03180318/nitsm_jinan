package com.jcca.web.asset.controller.bean;

import com.jcca.admin.system.entity.SysFile;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * 专家知识库对象 repository
 *
 * @author sophia
 * @date 2023-07-12
 */
@Data
public class Repository implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 故障名称
     */
    private String name;

    /**
     * 关键字
     */
    private String keyWord;

    /**
     * 资产类型
     */
    private String type;

    /**
     * 资产厂商
     */
    private String manufacturer;

    /**
     * 资产型号
     */
    private String model;

    /**
     * 操作系统
     */
    private String operationSystem;

    /**
     * 原始报文
     */
    private String msg;

    /**
     * 故障现象
     */
    private String casePhenomena;

    /**
     * 解决方案
     */
    private String plan;


    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改时间
     */
    private Timestamp updateTime;

    /**
     * 修改人
     */
    private String updateBy;


    /**
     * 相关附件
     */
    private List<SysFile> files;


    /**
     * 相关附件
     */
    private String log;

    /**
     * 是否强制录入
     */
    private String coerce = "false";

}
