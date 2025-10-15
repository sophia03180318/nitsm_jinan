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
 * @description: 屏蔽黑名单设备
 * @date 2025-10-14 星期二 10:43:48
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "ALARM_WHITE_ASSET")
public class AlarmWhiteAsset extends Model<InspectTemplate> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 用户ID
     */
    @TableField("USER_ID")
    private String userId;

    /**
     * 黑名单列表ID
     */
    @TableField("WHITE_ID")
    private String whiteId;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 创建者
     */
    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;
}
