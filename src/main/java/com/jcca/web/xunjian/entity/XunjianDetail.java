package com.jcca.web.xunjian.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 巡检明细表
 *
 * @author hanwone
 * @date 2021-02-24 16:11:47
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("xunjian_detail")
public class XunjianDetail extends Model<XunjianDetail> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 正常标记
     */
    public static final Integer NORMAL_FLAG = 0;
    /**
     * 异常标记
     */
    public static final Integer EXCEPTION_FLAG = 1;

    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 巡检记录ID
     */
    @TableField("XUNJIAN_RECORD_ID")
    private String xunjianRecordId;
    /**
     * 设备ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 设备ID
     */
    @TableField("ASSET_MODE")
    private Integer assetMode;
    /**
     * 设备名称
     */
    @TableField("ASSET_NAME")
    private String assetName;
    /**
     * 巡检指标项
     * XunJianTargetEnum
     */
    @TableField("XUNJIAN_TARGET_ITEM")
    private String xunjianTargetItem;
    /**
     * 阈值设定
     */
    @TableField("THRESHOLD_VALUE")
    private String thresholdValue;
    /**
     * 巡检结果值
     * V2放异常的信息 现状
     */
    @TableField("XUNJIAN_VALUE")
    private String xunJianValue;
    /**
     * 巡检结果描述
     * V2版本放巡检命令
     */
    @TableField("RESULT_MSG")
    private String resultMsg;
    /**
     * 有无异常，0正常，1异常
     */
    @TableField("NORMAL_FLAG")
    private Integer normalFlag;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;


}