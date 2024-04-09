package com.jcca.web.ip.vo;

import lombok.Data;

import java.util.Date;

/**
 * @ Author：sophia
 * @ Date：Created in 16:01 2021/11/3
 * @ Description:
 */
@Data
public class SysOrgVo {

    /**
     * 组织ID
     */
    private String id;

    /**
     * ip段名称
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
     * 组织状态
     */
    private Byte status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * 修改者
     */
    private String modifier;

    /**
     * 网关
     */
    private String gateway;

    /**
     * 网络名称
     */
    private String name;

    /**
     * 网络地址
     */
    private String netIp;

    /**
     * 掩码
     */
    private String mask;

    /**
     * 组织名称
     */
    private String OrgName;

}
