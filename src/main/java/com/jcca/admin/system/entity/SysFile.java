package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 文件记录表
 *
 * @author hanwone
 * @date 2020-04-06 12:14:22
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_file")
public class SysFile extends Model<SysFile> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 文件ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 文件原名
     */
    @TableField("ORIGN_NAME")
    private String orignName;
    /**
     * 文件名称
     */
    @TableField("FILE_NAME")
    private String fileName;
    /**
     * 文件路径
     */
    @TableField("FILE_PATH")
    private String filePath;
    /**
     * 文件类型
     */
    @TableField("MIME")
    private String mime;
    /**
     * 文件大小
     */
    @TableField("FILE_SIZE")
    private Long fileSize;
    /**
     * 文件夹ID
     */
    @TableField("FOLDER_ID")
    private String folderId;
    /**
     * MD5
     */
    @TableField("MD5")
    private String md5;
    /**
     * SHA1
     */
    @TableField("SHA1")
    private String sha1;
    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;
    /**
     * 文件状态
     * StatusEnum
     */
    @TableField("STATUS")
    private Byte status;
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