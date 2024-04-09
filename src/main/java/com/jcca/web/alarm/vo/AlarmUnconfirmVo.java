package com.jcca.web.alarm.vo;

import lombok.Data;

/**
 * @ClassName AlarmUnconfirmVo
 * @Description 未确认告警列表
 * @Date 2020/6/18 10:36
 * @Author hanwone
 */
@Data
public class AlarmUnconfirmVo {
    /**
     * 显示恢复
     */
    public static final Integer SHOW_RECOVER = 1;
    /**
     * 不显示恢复
     */
    public static final Integer SHOW_RECOVER_NO = 0;

    /**
     * 告警ID
     */
    private String id;
    /**
     * 告警标题
     */
    private String title;
    /**
     * 告警级别
     */
    private Byte alarmLevel;
    /**
     * 告警内容
     */
    private String content;
    /**
     * 告警发生时间
     */
    private String occurTime;
    /**
     * 告警类型
     */
    private Byte type;
    /**
     * 处理状态
     */
    private Byte status;
    /**
     * 天空告警标记,1正常，2天窗
     * AlarmBlankConst
     */
    private Byte blank;
    /**
     * 告警状态 1告警，2恢复
     */
    private Byte alarmState;
    /**
     * 告警次数
     */
    private Integer times;
    /**
     * 资产告警详情里是否显示'[已恢复]'字样 , 默认显示,返回数据时具体判断是否显示
     * 0不显示,  1显示
     * syt 2021/5/17
     */
    private Integer isShowRecover;
}
