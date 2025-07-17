package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author: hhw
 * @description: InspectRecordShare 主要是用来处理巡检记录的分享
 * @date: 2025-07-17  13:22
 * @since: 2.1.8.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("INSPECT_RECORD_SHARE")
public class InspectRecordShare extends Model<InspectRecordShare> {
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    private String jobId;
    private String inspectRecordId;
    private String operator;
    private String viewer;
    private String remark;

    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(value = "MODIFY_TIME", fill = FieldFill.UPDATE)
    private Date modifyTime;

    @TableField(value = "MODIFIER", fill = FieldFill.UPDATE)
    private String modifier;

    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;
}
