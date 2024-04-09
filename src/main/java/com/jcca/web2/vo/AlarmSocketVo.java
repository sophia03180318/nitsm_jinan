package com.jcca.web2.vo;

import com.jcca.web2.constant.Web2Const;
import lombok.Data;

/**
 * @description: 告警推送数据
 * @author: Lvyp
 * @create: 2023/11/16 09:33
 */
@Data
public class AlarmSocketVo {

    public static final Integer YES = 1;
    public static final Integer NO = 2;

    private String code = Web2Const.STATISTICS_TOP_MSG_ALARM;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 是否是中航设备
     */
    private Boolean isJcca;
    /**
     * 告警级别
     */
    private Integer alarmLevel;
    /**
     * 告警标题
     */
    private String alarmTitle;
    /**
     * 确认状态 1未确认，2已确认
     * AlarmStatusEnum
     */
    private Byte status;
    /**
     * 告警状态  1告警，2恢复
     * AlarmStateEnum
     */
    private Byte alarmState;
    /**
     * 是否需要弹出框
     * 如果是 空 则根据配置决定
     * 1弹出2不弹
     */
    private Integer popup;
    /**
     * 是否是新产生的告警
     * 1新的  2旧的
     */
    private Integer newAlarm;


}
