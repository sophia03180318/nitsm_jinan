package com.jcca.admin.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Date;

/**
 * 业务消息模板
 *
 * @author hanwone
 * @date 2020-04-23 15:52:15
 **/
@Data
@TableName("alarm_template")
public class AlarmTemplate extends Model<AlarmTemplate> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 模板名称
     */
    @TableField("NAME")
    @NotEmpty(message = "模板名称不能为空")
    private String name;
    /**
     * 模板标题
     */
    @TableField("TITLE")
    @NotEmpty(message = "模板标题不能为空")
    private String title;
    /**
     * 模板内容
     */
    @TableField("CONTENT")
    @NotEmpty(message = "模板内容不能为空")
    private String content;
    /**
     * 消息分类
     */
    @TableField("CATEGORY")
    @NotEmpty(message = "模板类别不能为空")
    private String category;
    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;
    /**
     * 模板状态
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