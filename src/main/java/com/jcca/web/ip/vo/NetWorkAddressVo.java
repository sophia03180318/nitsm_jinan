package com.jcca.web.ip.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 网络信息
 *
 * @author sophia
 */
@Data
public class NetWorkAddressVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

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
     * 掩码位数
     */
    private Integer maskNumber;

    /**
     * 掩码
     */
    private String mask;

    /**
     * ip数量
     */
    private Integer ipNumber;

    /**
     * 组织ID
     */
    private String OrgId;

    /**
     * 组织名称
     */
    private String OrgName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


    /**
     *IP通且已申请
     */
    private Integer usedAndFinish;

    /**
     *IP通且未未申请
     */
    private Integer usedAndunFinish;

    /**
     *IP不通且已申请
     */
    private Integer unusedAndFinish;

    /**
     *IP不通且未未申请
     */
    private Integer unusedAndunFinish;

    /**
     *已占用
     */
    private Integer used;

    /**
     *未占用
     */
    private Integer unused;

    /**
     * 已占用百分比
     */
    private double situationUse;

}
