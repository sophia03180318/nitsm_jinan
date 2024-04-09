package com.jcca.web.alarm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 告警知识库
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ALARM_REPOSITORY")
public class AlarmRepository extends Model<AlarmRepository> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 告警级别
     * AlarmLevel
     */
    @TableField(value = "ALARM_LEVEL", updateStrategy = FieldStrategy.IGNORED)
    private Integer alarmLevel;
    /**
     * 知识库匹配码
     */
    @TableField("ALARM_CODE")
    private String alarmCode;
    /**
     * 状态匹配码
     * 包含匹配
     */
    @TableField("STATUS_FLAG")
    private String statusFlag;
    /**
     * 状态匹配码的类型
     * EventLevelEnum
     */
    @TableField("FLAG_TYPE")
    private Integer flagType;
    /**
     * 事件类型ID
     */
    @TableField(value = "EVENT_TYPE_ID", updateStrategy = FieldStrategy.IGNORED)
    private String eventTypeId;
    /**
     * 描述
     */
    @TableField(value = "DESC_STR", updateStrategy = FieldStrategy.IGNORED)
    private String descStr = "";
    /**
     * 解决方案
     */
    @TableField(value = "PLAN_STR", updateStrategy = FieldStrategy.IGNORED)
    private String planStr = "";
    /**
     * 模板字符串
     */
    @TableField("template_str")
    private String templateStr;
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
    /**
     * 修改时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;


    public String toData() {
        return alarmCode + ",," + flagType;
    }
}
