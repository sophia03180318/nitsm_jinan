package com.jcca.web.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 告警事件关联表
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ALARM_EVENT_REL")
public class AlarmEventRel extends Model<AlarmEventRel> implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 告警ID
     */
    @TableField("ALARM_ID")
    private String alarmId;
    /**
     * 事件ID
     */
    @TableField("EVENT_ID")
    private String eventId;
    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    private Date createTime;
}
