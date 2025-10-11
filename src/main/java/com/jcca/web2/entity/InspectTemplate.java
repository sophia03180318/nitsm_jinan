package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lifp
 * @version 1.0
 * @description: 智能巡检模板
 * @date 2025-10-09 星期四 10:41:37
 */
@EqualsAndHashCode(callSuper = true)
@TableName(value = "INSPECT_TEMPLATE")
@Data
public class InspectTemplate extends Model<InspectTemplate> implements Serializable {

    private static final long serialVersionUID = -2265325783573462872L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    private String assetId;
    private String templateName;
    private String userId;
    private String templateCode;

    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;
}
