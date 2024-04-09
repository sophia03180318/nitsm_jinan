package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @description: 业务类型
 * @author: sophia
 * @create: 2023/11/01 15:12
 **/
@EqualsAndHashCode(callSuper = true)
@TableName("BUSINESSSERVICE_SERVICE_TYPE")
@Data
public class BusinessServiceType extends Model<BusinessServiceType> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 类别名称
     */
    @TableField("NAME")
    private String name;

    /**
     * 业务类型 BusinessTypeEnums
     * 字典 BUSINESS_TYPE
     */
    @TableField("BUSINESS_TYPE")
    private Integer businessType;

    /**
     * 顺序
     */
    @TableField("SORT")
    private Integer sort = 1;

    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

}