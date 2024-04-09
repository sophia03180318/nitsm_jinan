package com.jcca.web.broken.controller.bean;

import com.jcca.web.broken.entity.BrokenRecordOpinion;
import lombok.Data;

import java.util.List;

/**
 * 故障记录，故障案例表
 *
 * @author hanwone
 * @date 2021-03-16 09:37:52
 **/
@Data
public class BrokenRecordWord implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 告警ID
     */

    private String id;
    /**
     * 告警状态，1告警，2恢复
     */
    private Long alarmState;
    /**
     * 告警确认状态，1未确认，2已确认
     */
    private Long alarmStatus;
    /**
     * 告警处理状态处理状态,1未处理，2已处理
     */
    private Long solveStatus;

    /**
     * 告警级别
     * syt
     */
    private Long alarmLevel;
    /**
     * 告警分类
     * AlarmTemplateEnum
     */
    private String title;

    /**
     * 告警内容
     */
    private String content;
    /**
     * 告警处理记录
     */
    private List<BrokenRecordOpinion> brokenRecordOpinions;

}