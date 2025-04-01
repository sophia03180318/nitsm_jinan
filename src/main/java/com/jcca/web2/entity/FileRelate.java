package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author HanHW
 * @description 文件关系表
 * @className FileRelate
 * @date 2025/4/1 16:34
 * @since 2.1.5.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("FILE_RELATE")
public class FileRelate extends Model<FileRelate> {
    private static final long serialVersionUID = 526992227688731901L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 元素ID，元素可以是机柜ID，可以是设备ID
     */
    private String itemId;
    /**
     * sys_file id
     */
    private String fileId;
    /**
     * 元素类型，OrgTypeConst
     */
    private Integer itemType;
    /**
     * 元素名称
     */
    private Integer itemName;
    /**
     * 备注
     */
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
