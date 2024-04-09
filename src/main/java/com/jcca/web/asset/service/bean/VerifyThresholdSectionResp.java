package com.jcca.web.asset.service.bean;

import lombok.Data;

/**
 * 设定区间阈值的响应
 */
@Data
public class VerifyThresholdSectionResp {

    /**
     * 是否开启一级告警
     */
    private boolean oneLevelEvent;
    /**
     * 是否开启二级告警
     */
    private boolean twoLevelEvent;
    /**
     * 是否开启三级告警
     */
    private boolean threeLevelEvent;
    /**
     * 一级告警状态
     */
    private boolean oneLevelEventStatus;
    /**
     * 二级告警状态
     */
    private boolean twoLevelEventStatus;
    /**
     * 三级告警状态
     * false 告警  true恢复
     */
    private boolean threeLevelEventStatus;
    /**
     * 一级时间匹配码
     */
    private String oneEventCode;
    /**
     * 二级时间匹配码
     */
    private String twoEventCode;
    /**
     * 三级时间匹配码
     */
    private String threeEventCode;

    /**
     * 一级告警信息
     */
    private String oneEventMsg;
    /**
     * 二级告警信息
     */
    private String twoEventMsg;
    /**
     * 三级告警信息
     */
    private String threeEventMsg;

    /**
     * 一级设置值
     */
    private String oneEventValue;
    /**
     * 二级设置值
     */
    private String twoEventValue;
    /**
     * 三级设置值
     */
    private String threeEventValue;


    /**
     * 开启或关闭阶段告警
     *
     * @param close
     */
    public void closeLevelEvent(boolean close) {
        this.oneLevelEvent = close;
        this.twoLevelEvent = close;
        this.threeLevelEvent = close;
    }


    /**
     * 1阶发生告警
     *
     * @param eventUniqueCode
     */
    public void openOneEventAlarm(String eventUniqueCode, Integer confNum, String msg) {
        this.oneEventCode = eventUniqueCode;
        this.oneLevelEventStatus = false;
        this.oneLevelEvent = true;
        this.oneEventValue = confNum.toString();
        this.oneEventMsg = msg;

    }

    /**
     * 2阶发生告警
     */
    public void openTwoEventAlarm(String eventUniqueCode, Integer confNum, String msg) {
        this.twoEventCode = eventUniqueCode;
        this.twoLevelEventStatus = false;
        this.twoLevelEvent = true;
        this.twoEventValue = confNum.toString();
        this.twoEventMsg = msg;
    }

    /**
     * 3阶关闭告警
     */
    public void openThreeEventAlarm(String eventUniqueCode, Integer confNum, String msg) {
        this.threeEventCode = eventUniqueCode;
        this.threeLevelEventStatus = false;
        this.threeLevelEvent = true;
        this.threeEventValue = confNum.toString();
        this.threeEventMsg = msg;
    }

    /**
     * 3阶告警恢复
     */
    public void closeThreeEventAlarm(String eventUniqueCode, String msg, Integer configNum) {
        this.threeEventCode = eventUniqueCode;
        this.threeLevelEventStatus = true;
        this.threeLevelEvent = true;
        this.threeEventMsg = msg;
        this.threeEventValue = configNum.toString();
    }

    /**
     * 2阶告警恢复
     */
    public void closeTwoEventAlarm(String eventUniqueCode, String msg, Integer configNum) {
        this.twoEventCode = eventUniqueCode;
        this.twoLevelEventStatus = true;
        this.twoLevelEvent = true;
        this.twoEventMsg = msg;
        this.twoEventValue = configNum.toString();
    }

    /**
     * 关闭一阶告警
     *
     * @param eventUniqueCode
     */
    public void closeOneEventAlarm(String eventUniqueCode, String msg, Integer configNum) {
        this.oneEventCode = eventUniqueCode;
        this.oneLevelEventStatus = true;
        this.oneLevelEvent = true;
        this.oneEventMsg = msg;
        this.oneEventValue = configNum.toString();
    }
}
