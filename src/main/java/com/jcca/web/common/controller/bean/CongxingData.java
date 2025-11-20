package com.jcca.web.common.controller.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author SOPHIA
 * @description 通信质量监督数据
 * @className CongxingData
 * @date 2025/10/16 10:26
 * @since 2.0.5.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CongxingData extends Model<CongxingData> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 全局唯一事件ID
     */
    private String eventId;

    /**
     * 告警类型编码
     */
    private Integer alarmCode;

    /**
     * 告警标题
     */
    private String alarmTitle;

    /**
     * 告警级别
     * <p>表示告警的严重程度。</p>
     */
    private Integer alarmLevel;

    /**
     * 告警源系统标识
     * <p>表示产生告警的来源系统。</p>
     */
    private String sourceSystem;

    private String sourceIp;

    /**
     * 告警源对象名称
     */
    private String sourceObject;

    /**
     * 告警触发时间
     */
    private Date occurTime;

    /**
     * 告警恢复时间
     */
    private Date recoverTime;

    /**
     * 告警描述
     */
    private String alarmDescription;

    /**
     * 告警详情（扩展字段）
     */
    private String alarmDetails;

    /**
     * 告警状态，0报警发生，1报警恢复
     */
    private Integer alarmStatus;


    private String alarmTitleStr;


}