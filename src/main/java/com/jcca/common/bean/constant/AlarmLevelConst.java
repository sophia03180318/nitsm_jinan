package com.jcca.common.bean.constant;

/**
 * @ClassName AlarmLevelConst
 * @Description 告警级别常量
 * @Date 2020/5/20 10:45
 * @Author hanwone
 */
public interface AlarmLevelConst {


    /**
     * 一级告警
     */
    Integer SERIOUS_LEVEL = 1;
    /**
     * 二级告警
     */
    Integer MIDDLE_LEVEL = 2;
    /**
     * 三级告警
     */
    Integer LIGHT_LEVEL = 3;
    /**
     * 信息通知
     */
    Integer MSG_LEVEL = 4;
    /**
     * 未配制
     */
    Integer UN_CONFIG = 9;
}
