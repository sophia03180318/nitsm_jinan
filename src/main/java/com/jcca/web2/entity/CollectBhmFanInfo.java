package com.jcca.web2.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 风扇
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_BHM_FAN_INFO")
public class CollectBhmFanInfo  extends Model<CollectBhmFanInfo> {


    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 成员ID 代表风扇的槽位
     * 第几个风扇
     */
    @TableField("MEMBER_ID")
    private String memberId;
    /**
     * 部件编号
     */
    @TableField("PART_NUMBER")
    private String partNumber;
    /**
     * 当前转速
     */
    @TableField("READING")
    private Integer reading;
    /**
     * 转速单位
     */
    @TableField("READING_UNITS")
    private String readingUnits;
    /**
     * 最大转速
     *  "RPM"(转 / 分钟)
     */
    @TableField("MAX_READING_RANGE")
    private Integer maxReadingRange;
    /**
     * 最小转速
     * "RPM"(转 / 分钟)
     */
    @TableField("MIN_READING_RANGE")
    private Integer minReadingRange;
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
