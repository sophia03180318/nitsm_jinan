package com.jcca.web.alarm.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 告警信息
 *
 * @author Lvyp
 */
@Data
public class AlarmInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    /**
     * 告警标题
     */
    private String title;
    /**
     * 告警级别 AlarmLevelEnum
     */
    private Byte alarmLevel;
    /**
     * 告警级别
     */
    private String levelStr;
    /**
     * 告警状态 AlarmStatusEnum
     */
    private Byte status;
    /**
     * 告警状态
     */
    private String statusStr;
    /**
     * 告警类型 AlarmTypeEnum
     */
    private Byte type;
    /**
     * 关联ID
     */
    private String correlationId;
    /**
     * 告警类型
     */
    private String typeStr;
    /**
     * 产生时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date occurTime;
    /**
     * 转换后的产生时间
     * 距离当前时间的时差
     */
    private String occurTimeStr;
    /**
     * 告警次数
     */
    private Integer times;
    /**
     * 是否已经转成故障记录
     */
    private Byte alarmToRecord;
    /**
     * 资产id
     */
    private String assetId;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产IP
     */
    private String ip;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 天窗告警标记，1正常时段告警，2天窗时段告警
     * AlarmBlankConst
     */
    private Byte blank;
    /**
     * 告警恢复状态，1告警状态，2已恢复状态
     */
    private String alarmStateStr;
    /**
     * 告警内容
     */
    private String content;
    /**
     * 告警分类
     */
    private String alarmCategory;
    /**
     * sysLog标记
     */
    private String sysLogFlag;

    private Integer isShowRecover;
}
