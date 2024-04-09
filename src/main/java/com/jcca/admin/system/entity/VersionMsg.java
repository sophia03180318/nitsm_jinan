package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 版本信息
 *
 * @author lyp
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("M_VERSION_MSG")
public class VersionMsg extends Model<VersionMsg> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 文件版本
     */
    @TableField("VERSION")
    private String version;
    /**
     * 文件描述
     */
    @TableField("DESC_STR")
    private String descStr;
    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;
    /**
     * 文件ID
     */
    @TableField("SYS_FILE_ID")
    private String sysFileId;
    /**
     * 文件保存地址
     */
    @TableField("FILE_PATH")
    private String filePath;
    /**
     * 文件MD5
     */
    @TableField("MD5")
    private String md5;
    /**
     * 创建时间
     */
    @TableField("CREATE_DATE")
    private Date createDate;

    @TableField(exist = false)
    private String createDateStr;

}
