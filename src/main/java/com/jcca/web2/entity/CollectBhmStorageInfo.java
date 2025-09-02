package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 存储组信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_BHM_STORAGE_INFO")
public class CollectBhmStorageInfo extends Model<CollectBhmStorageInfo> {

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 内部硬件ID
     */
    @TableField("NUMBER_ID")
    private String numberId;

    @TableField("ASSET_ID")
    private String assetId;

    @TableField("NAME")
    private String Name;

    /**
     * 健康状态
     */
    @TableField("HEALTH")
    private String health;
    /**
     * 是否启用
     * Enabled 启用
     */
    @TableField("STATE")
    private String state;

    /**
     * 采集批次码
     */
    @TableField("COLLECT_CODE")
    private String collectCode;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

}
