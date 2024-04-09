package com.jcca.web.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 自定义的事件组告警
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ALARM_EVENT_GROUP")
public class AlarmEventGroup extends Model<AlarmEventGroup> implements Serializable {

    public static final String SPLIT_FLAG = "-";

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 告警组名字
     */
    @TableField("NAME")
    private String name;
    /**
     * 告警类型
     * AlarmTypeEnum
     */
    @TableField("ALARM_TYPE")
    private Integer alarmType;
    /**
     * 事件类型ID组合
     * -分割
     */
    @TableField("EVENT_TYPE_IDS")
    private String eventTypeIds;
    /**
     * 告警级别
     */
    @TableField("LEVLE")
    private Integer levle;
    /**
     * 告警模板
     */
    @TableField("ALARM_MSG_TEMP")
    private String msgTemp;
    /**
     * 告警是否可恢复标识
     * 1可以-1不可以
     * EventRecoverFlagEnum
     */
    @TableField("RECOVER_FLAG")
    private Integer recoverFlag;
    /**
     * 逻辑与或关系标识
     * EventGroupLogicEnum
     */
    @TableField("LOGICAL_FLAG")
    private Integer logicalFlag;
    /**
     * 启用阶段告警
     */
    @TableField("USE_STAGE")
    private Integer useStage;
    /**
     * 阶段告警配置
     */
    @TableField("STAGE_CONFIG")
    private String stageConfig;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "CREATE_DATE")
    private Date createDate;

}
