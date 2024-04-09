package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;


/**
 * @ Author：sophia
 * @ Date：Created in 16:09 2021/6/22
 * @ Description:资源导入模板
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "IMPORT_TEMPLATE")
public class ImportTemplate extends Model<ImportTemplate> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 模板ID
     */
    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 模板名称
     */
    @TableField("TEMPLATE_NAME")
    private String templateName;
    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;
    /**
     * 模板类型
     */
    @TableField("TYPE")
    private String type;

    /**
     * 具有属性
     */
    @TableField("ATTRIBUTE")
    private String attribute;


    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;

}
