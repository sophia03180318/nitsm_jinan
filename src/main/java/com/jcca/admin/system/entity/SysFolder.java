package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 文件夹表
 *
 * @author hanwone
 * @date 2020-05-18 13:46:10
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_folder")
public class SysFolder extends Model<SysFolder> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 维护手册根文件夹id
     */
    public static final String FOLDER_ROOT_ID = "0";
    /**
     * 共享云盘根文件夹id
     */
    public static final String FOLDER_ROOT_DISK_ID = "1";


    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 名称
     */
    @TableField("TITLE")
    private String title;
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
     * 目录类别
     * FolderCategoryEnum
     */
    @TableField("CATEGORY")
    private Byte category;
    /**
     * 状态
     * StatusEnum
     */
    @TableField("STATUS")
    private Byte status;
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