package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author HanHW
 * @description 阈值管理
 * @className ThresholdManage
 * @date 2023/12/15 14:32
 * @since 2.1.0.0
 */
@Data
@TableName("THRESHOLD_MANAGE")
public class ThresholdManage {

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 组织ID
     */
    private String orgId;

    /**
     * 资产ID
     */
    private String assetId;

    /**
     * 资产类型
     */
    private Integer assetDesk;

    /**
     * 业务类型ID
     */
    private String serviceTypeId;
    /**
     * 阈值分类 CPU，内存等
     * 字典 THRESHOLD_CATEGORY
     * 枚举 ThresholdCategoryEnum
     */
    private String category;
    /**
     * 普通阈值
     */
    private Double general;
    /**
     * 范围阈值下限
     */
    private Double rangeMin;
    /**
     * 范围阈值上限
     */
    private Double rangeMax;
    /**
     * 阶梯阈值备用
     */
    private Double stepLow;
    /**
     * 阶梯阈值高
     */
    private Double stepHigh;
    /**
     * 阶梯阈值较高
     */
    private Double stepHigher;
    /**
     * 阶梯阈值最高
     */
    private Double stepHighest;
    /**
     * 0不可用，1可用
     */
    private Integer onAlarm;
    /**
     * 1组织，2单个
     * ThresholdAutoFlagEnum
     */
    private Integer autoFlag;

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

    @TableField(exist = false)
    private String assetImage;
    @TableField(exist = false)
    private List<Integer> desks;
    @TableField(exist = false)
    private List<String> orgIds;
    @TableField(exist = false)
    private List<String> assetIds;
    /**
     * 覆盖单个阈值
     * 0不覆盖，1覆盖
     */
    @TableField(exist = false)
    private Integer reset;
}
