package com.jcca.web.ip.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 网络列表
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("M_NET_WORK_ADDRESS")
public class NetWorkAddress extends Model<NetWorkAddress> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 网络名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 网关
     */
    @TableField("GATEWAY")
    private String gateway;
    /**
     * 网络地址
     */
    @TableField("NET_IP")
    private String netIp;
    /**
     * 掩码位数
     */
    @TableField("MASK_NUMBER")
    private Integer maskNumber;
    /**
     * 掩码
     */
    @TableField("MASK")
    private String mask;

    /**
     * 所属组织Id
     */
    @TableField("ORG_ID")
    private String orgId;

    /**
     * 起始ip
     */
    @TableField("START_IP")
    private String startIp;

    /**
     * 终止ip
     */
    @TableField("END_IP")
    private String endIp;

    /**
     * ip数量
     */
    @TableField("IP_NUMBER")
    private Integer ipNumber;
    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 创建者
     */
    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;
    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

}
