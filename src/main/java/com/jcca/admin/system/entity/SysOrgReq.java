package com.jcca.admin.system.entity;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
    @NotEmpty(message = "组织名称不能为空")
    private String title;
    @NotNull(message = "父级组织不能为空")
    private String pid;
    @NotNull(message = "组织类型不能为空")
    private Integer type;
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