package com.jcca.web.construction.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 施工记录
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("construction_record")
public class ConstructionRecord extends Model<ConstructionRecord> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 施工记录名称
     */
    @NotBlank(message = "请输入施工记录名称")
    @TableField("NAME")
    private String name;
    /**
     * 施工开始时间
     */
    @NotNull(message = "请选择开始时间")
    @TableField("START_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    /**
     * 施工结束时间
     */
    @NotNull(message = "请选择结束时间")
    @TableField("END_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    /**
     * 施工地点
     */
    @TableField("PLACE")
    private String place;
    /**
     * 施工人
     */
    @TableField("OPERATOR")
    private String operator;
    /**
     * 配合人
     */
    @TableField("COOPERATOR")
    private String cooperator;
    /**
     * 影响范围
     * 资产id,链接
     */
    @NotEmpty(message = "请选择影响范围")
    @TableField("INFLUENCE")
    private String influence;
    /**
     * 备注信息
     */
    @TableField("REMARK")
    private String remark;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;
    /**
     * 所属组织ID
     */
    @TableField(value = "ORG_ID")
    private String orgId;

}
