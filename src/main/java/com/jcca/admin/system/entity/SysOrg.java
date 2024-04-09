package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 组织表
 *
 * @author hanwone
 * @date 2020-04-06 12:13:28
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_org")
public class SysOrg extends Model<SysOrg> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 组织ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 组织名称
     */
    @TableField("TITLE")
    private String title;

    @TableField(exist = false)
    private String name;

    /**
     * 组织类型
     */
    @TableField("TYPE")
    private Integer type;
    /**
     * 父级ID
     */
    @TableField("PID")
    private String pid;
    /**
     * 所有父级ID
     */
    @TableField("PIDS")
    private String pids;
    /**
     * 排序
     */
    @TableField("SORT")
    private Byte sort;
    /**
     * 组织状态
     */
    @TableField("STATUS")
    private Byte status;
    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;
    /**
     * 车站X坐标
     */
    @TableField("STATIONX")
    private String stationx;
    /**
     * 车站Y坐标
     */
    @TableField("STATIONY")
    private String stationy;
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
     * 组织下是否有机房
     * 有机房：true，没有机房false
     */
    @TableField(exist = false)
    private Boolean existRoom;

    /**
     * 组织下是否是新增
     */
    @TableField(exist = false)
    private Boolean existAdd;

    void sysOrg(String id) {
        this.id = id;
    }

}