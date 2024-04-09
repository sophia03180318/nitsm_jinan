package com.jcca.web.xunjian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 巡检信息
 *
 * @author Lvyp
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("xunjian_alarm_msg")
public class XunjianAlarmMsg extends Model<XunjianAlarmMsg> {

    private static final long serialVersionUID = 1L;

    /**
     * 数据ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    @TableField("ALARM_INFO_ID")
    private String alarmInfoId;
    /**
     * 巡检记录ID
     */
    @TableField("XUNJIAN_RECORD_ID")
    private String xunjianRecordId;
    /**
     * 告警标题
     */
    @TableField("TITLE")
    private String title;
    /**
     * 告警级别 AlarmLevelEnum
     */
    @TableField("ALARM_LEVEL")
    private Byte alarmLevel;
    /**
     * 确认状态 AlarmStatusEnum
     */
    @TableField("STATUS")
    private Byte status;
    /**
     * 告警状态 AlarmStateEnum
     */
    @TableField("ALARM_STATE")
    private Byte alarmState;
    /**
     * 告警类别 ReceiveAlarmTypeEnum
     */
    @TableField("ALARM_CATEGORY")
    private String alarmCategory;
    /**
     * 资产id
     */
    @TableField("ASSET_ID")
    private String assetId;
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
     * 天窗告警标记，1正常时段告警，2天窗时段告警
     * AlarmBlankConst
     */
    @TableField("BLANK")
    private Byte blank;

    @TableField(value = "CREATE_TIME")
    private Date createDate;

}
