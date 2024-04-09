package com.jcca.web.broken.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 故障记录
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("BROKEN_RECORD")
public class BrokenRecord extends Model<BrokenRecord> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 故障记录id
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产id
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 告警ID
     */
    @TableField("ALARM_ID")
    private String alarmId;

    @TableField("ALARM_TITLE")
    private String alarmTitle;
    /**
     * 故障发生时间
     */
    @TableField("OCCUR_TIME")
    private Date occurTime;
    /**
     * 处理完成时间
     */
    @TableField("COMPLETE_TIME")
    private Date completeTime;
    /**
     * 等级
     */
    @TableField("ALARM_LEVEL")
    private Byte alarmLevel;
    /**
     * 故障来源，1手工录入，2告警转换
     */
    @TableField("ORIGIN")
    private Byte origin;
    /**
     * 故障现象描述
     */
    @TableField("DESCRIPTION")
    private String description;
    /**
     * 影响范围
     */
    @TableField("INFLUENCE")
    private String influence;
    /**
     * 故障原因
     */
    @TableField("REASON")
    private String reason;
    /**
     * 处理结果
     */
    @TableField("HANDLE_RESULT")
    private String handleResult;
    /**
     * 处理方法
     */
    @TableField("HANDLE_WAY")
    private String handleWay;
    /**
     * 状态
     * BrokenRecordConst
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
