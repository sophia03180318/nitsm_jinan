package com.jcca.web.alarm.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName AlarmDetailVo
 * @Description 告警详情
 * @Date 2020/6/17 11:43
 * @Author hanwone
 */
@Data
public class AlarmDetailVo {

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
     * 告警状态
     */
    private Byte status;
    /**
     * 告警时间
     */
    private String occurTimeStr;
    /**
     * 告警类型
     */
    private String typeStr;
    /**
     * 资产IP
     */
    private String ip;
    /**
     * 组织id
     */
    private String orgId;
    private String orgName;
    /**
     * 机柜名称
     */
    private String cabinetName;
    /**
     * 机房名称
     */
    private String roomName;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 告警内容
     */
    private String content;
    /**
     * 解决方案
     */
    private String description;
    /**
     * 解决方案列表
     */
    private List<String> descriptionList;
    /**
     * 备注
     */
    private String remark;
    /**
     * 处理意见
     */
    private String opinion;
    /**
     * 资产类型
     */
    private String assetMode;
    /**
     * 资产型号
     */
    private String assetImage;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 是否转故障记录
     */
    private Byte alarmToRecord;
    /**
     * 天窗告警标记，1正常时段告警，2天窗时段告警
     * AlarmBlankConst
     */
    private Byte blank;

    /**
     * 告警编号
     */
    private String alarmCode;

    private Integer isShowRecover;
}
