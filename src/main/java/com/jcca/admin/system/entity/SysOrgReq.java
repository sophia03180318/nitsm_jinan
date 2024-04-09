package com.jcca.admin.system.entity;

import lombok.Data;

/**
 * 组织表
 *
 * @author hanwone
 * @date 2020-04-06 12:13:28
 **/
@Data
public class SysOrgReq implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 组织ID
     */
    private String id;
    /**
     * 组织名称
     */
    private String title;

    /**
     * 组织类型
     */
    private Integer type;
    /**
     * 父级ID
     */
    private String pid;
    /**
     * 所有父级ID
     */
    private String pids;
    /**
     * 排序
     */
    private Byte sort;
    /**
     * 备注
     */
    private String remark;
    /**
     * 机柜总行数
     */
    private Integer rowSize;
    /**
     * 每行机柜数量
     */
    private Integer amount;

}