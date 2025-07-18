package com.jcca.web.alarm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 告警信息
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ALARM_INFO")
public class AlarmInfo extends Model<AlarmInfo> implements Serializable {

    public static final Integer SHOW_RECOVER = 1;
    /**
     * 特殊标识，标识不会恢复的那部分告警
     */
    public static final Integer SHOW_RECOVER_NO_FLAG = 2;

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 告警标题
     */
    @TableField("TITLE")
    private String title;
    /**
     * 告警级别
     * AlarmLevelEnum
     */
    @TableField("ALARM_LEVEL")
    private Byte alarmLevel;
    /**
     * 确认状态 1未确认，2已确认
     * AlarmStatusEnum
     */
    @TableField("STATUS")
    private Byte status;
    /**
     * 告警状态  1告警，2恢复
     * AlarmStateEnum
     */
    @TableField("ALARM_STATE")
    private Byte alarmState;
    /**
     * 资产id
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 资产IP
     */
    @TableField("ASSET_IP")
    private String assetIp;
    /**
     * 资产名称
     */
    @TableField("ASSET_NAME")
    private String assetName;
    /**
     * 组织ID
     */
    @TableField("ORG_ID")
    private String orgId;
    /**
     * 告警类型
     * AlarmTypeEnum
     */
    @TableField("TYPE")
    private Integer type;
    /**
     * 产生时间
     */
    @TableField("OCCUR_TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date occurTime;
    /**
     * 最后一次告警时间
     */
    @TableField("LAST_TIME")
    private Date lastTime;
    /**
     * 确认时间
     */
    @TableField("CONFIRM_TIME")
    private Date confirmTime;
    /**
     * 确认人
     */
    @TableField("CONFIRMOR")
    private String confirmor;

    /**
     * 原始信息
     */
    @TableField("DESCRIPTION")
    private String description;
    /**
     * 告警内容
     */
    @TableField("CONTENT")
    private String content;
    /**
     * 是否已经转成故障记录
     * 0是未转  1转
     */
    @TableField("ALARM_TO_RECORD")
    private Byte alarmToRecord;
    /**
     * 解决方案
     */
    @TableField("OPINION")
    private String opinion;
    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;
    /**
     * 告警标识
     * 如磁盘名称、端口名称
     */
    @TableField("ALARM_FLAG")
    private String alarmFlag;
    /**
     * 告警标识 event:xxx:xxx
     */
    @TableField("ALARM_CODE")
    private String alarmCode;
    /**
     * 大类型  两级的event:event_xxx
     */
    @TableField("EVENT_CATEGORY")
    private String eventCategory;
    /**
     * 天窗告警标记，1正常时段告警，2天窗时段告警
     * AlarmBlankConst
     */
    @TableField("BLANK")
    private Byte blank;
    /**
     * 关联ID
     * 关联的事件组ID
     */
    @TableField("CORRELATION_ID")
    private String correlationId;
    /**
     * 是否显示'[已恢复]'字样
     * -1不显示  1显示
     */
    @TableField("IS_SHOW_RECOVER")
    private Integer isShowRecover = -1;
    /**
     * 0不追踪 1追踪  2追踪结束
     */
    @TableField("TRACE_STATUS")
    private Integer traceStatus;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 创建人
     */
    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;
    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;
    /**
     * 修改人
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

}
