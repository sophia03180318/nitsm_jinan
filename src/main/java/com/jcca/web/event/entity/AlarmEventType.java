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
 * 事件类型
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ALARM_EVENT_TYPE")
public class AlarmEventType extends Model<AlarmEventType> implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String SPLIT_FLAG = "T-_-T";

    /**
     * 事件类型ID
     */
    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 事件类型名称
     */
    @TableField(value = "NAME")
    private String name;
    /**
     * 事件类型
     */
    @TableField(value = "EVENT_CATEGORY")
    private String eventCategory;
    /**
     * 事件描述
     */
    @TableField(value = "DESC_STR")
    private String descStr;

    /**
     * 状态
     * EventTypeStatusEnum
     */
    @TableField(value = "STATUS")
    private Integer status;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "CREATE_TIME")
    private Date createTime;
}
