package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @description: 资产类型 183 201 ...
 * @author: sophia
 * @create: 2023/11/02 14:39
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class AssetMode extends Model<AssetMode> {

    private static final long serialVersionUID = 2336892678259808655L;
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 类型名
     */
    @TableField("NAME")
    private String name;

    /**
     * 小类型码
     */
    @TableField("CODE")
    private Integer code;

    /**
     * 大类型码
     */
    @TableField("AMODE")
    private Integer amode;

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