package com.jcca.web.xunjian.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 巡检记录表
 *
 * @author hanwone
 * @date 2021-02-24 16:12:09
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("xunjian_record_V2")
public class XunjianRecordV2 extends Model<XunjianRecordV2> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 巡视人
     */
    @TableField("OPERATOR")
    private String operator;
    /**
     * 班次
     */
    @TableField("XUNJIAN_SHIFT")
    private String xunjianShift;
    /**
     * 巡视时间
     */
    @TableField("XUNJIAN_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date xunjianTime;
    /**
     * 异常设备数量
     */
    @TableField("EXCEPTION_NUM")
    private Integer exceptionNum;
    /**
     * 正常设备数量
     */
    @TableField("NORMAL_NUM")
    private Integer normalNum;
    /**
     * 巡视内容
     */
    @TableField("XUNJIAN_TARGET")
    private String xunjianTarget;
    /**
     * 巡视内容
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
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;
    /**
     * 翻译后的指标描述
     */
    @TableField(exist = false)
    private String xunjianTargetStr;

}