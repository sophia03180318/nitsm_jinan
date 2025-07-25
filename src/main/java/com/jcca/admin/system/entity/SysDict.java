package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 字典表
 *
 * @author hanwone
 * @date 2020-04-06 12:14:00
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_dict")
public class SysDict extends Model<SysDict> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 字典ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 字典名称
     * 汉字名称
     */
    @TableField("TITLE")
    private String title;
    /**
     * 字典键名称
     * 编码
     */
    @TableField("NAME")
    private String name;
    /**
     * 字典类型
     */
    @TableField("TYPE")
    private Byte type;
    /**
     * 字典键值
     */
    @TableField("VALUE")
    private String value;
    /**
     * 字典状态
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

    public String toData() {
        return name + ",," + value;
    }
}