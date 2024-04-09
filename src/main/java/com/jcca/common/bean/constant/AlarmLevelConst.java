package com.jcca.common.bean.constant;

/**
 * @ClassName AlarmLevelConst
 * @Description 告警级别常量
 * @Date 2020/5/20 10:45
 * @Author hanwone
 */
public interface AlarmLevelConst {

    /**
     * 未知告警
     */
    byte UNKNOW = 0;
    /**
     * 一级告警
     */
    byte SERIOUS_LEVEL = 1;
    /**
     * 二级告警
     */
    byte MIDDLE_LEVEL = 2;
    /**
     * 三级告警
     */
    byte LIGHT_LEVEL = 3;
    /**
     * 信息通知
     */
    byte MSG_LEVEL = 4;
}
