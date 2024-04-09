package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 进程阈值
 *
 * @author syt
 * @date 2021/10/26 14:39
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("PROMPT_INFO")
@Component("PromptInfo")
public class PromptInfo extends Model<PromptInfo> {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 软件类型ID
     * BUSINESSSERVICE_SERVICE_TYPE 表ID
     */
    @TableField("SOFTWARETYPE_ID")
    private String softwareTypeId;
    /**
     * 进程名称
     */
    @TableField("VALUE")
    private String value;
    /**
     * 模板名称
     */
    @TableField("PLATE_NAME")
    private String plateName;
    /**
     * 模板ID
     */
    @TableField("PLATE_ID")
    private String plateId;
    /**
     * 进程备注
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

}
