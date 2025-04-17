package com.jcca.web.ip.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * ip信息
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("M_IP_INFO")
public class IpInfo extends Model<IpInfo> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 网络Id
     */
    @TableField("NET_WORK_ADDRESS_ID")
    private String netWorkAddressId;
    /**
     * 生成的IP
     */
    @TableField("IP")
    private String ip;
    /**
     * ping状态
     */
    @TableField("PING_STATUS")
    private Byte pingStatus;
    /**
     * 上次发起ping操作时间
     */
    @TableField("PING_DATE")
    private Date pingDate;

    /**
     * Mac地址
     */
    @TableField("MAC")
    private String mac;
    /**
     * 掩码
     */
    @TableField("MASK")
    private String mask;
    /**
     * 网关
     */
    @TableField("GATEWAY")
    private String gateway;
    /**
     * 状态
     */
    @TableField("STATUS")
    private Byte status;
    /**
     * 审核状态
     */
    @TableField("AUTH_STATUS")
    private Integer authStatus;
    /**
     * 审核人
     */
    @TableField("AUTH_USER_ID")
    private String authUserId;
    /**
     * 审核时间
     */
    @TableField("AUTH_DATE")
    private Date authDate;
    /**
     * 备注信息  11月14日更改为 此ip位置的设备ID
     */
    @TableField("REMARK")
    private String remark;
    /**
     * IP排序
     */
    @TableField("RANK")
    private Long rank;
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

    /**
     * 设备名称
     * 如果IP分配的有设备
     * 则会填充此字段
     */
    @TableField(exist = false)
    private String assetName;

}
