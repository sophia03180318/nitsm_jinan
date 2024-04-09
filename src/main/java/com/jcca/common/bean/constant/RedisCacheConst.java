package com.jcca.common.bean.constant;

/**
 * @ClassName RedisCacheConst
 * @Description redis缓存常量
 * @Date 2020/7/21 18:19
 * @Author hanwone
 */
public interface RedisCacheConst {

    /**
     * 告警各类缓存前缀
     */
    String ALARM_CATEGORY_PRE = "alarm:category:";
    /**
     * 设备阈值前缀
     */
    String THRESHOLD_ALARM_PRE = "threshold:alarm:";
    /**
     * 上月告警总数
     */
    String STATISTICS_LAST_MONTH_ALARM_ALL = "statistics:last:month:alarm:all:";
    /**
     * 上月一级告警总数
     */
    String STATISTICS_LAST_MONTH_ALARM_FIRST = "statistics:last:month:alarm:first:";

    /**
     * 光交换机采集数据
     */
    String OPTICAL_SWITCH_MSG = "_OPTICAL_SWITCH_MSG_";

    /**
     * 进程CPU占用率前5
     */
    String TOP5_PROCESS_CPU = "_TOP5_PROCESS_CPU:";

    /**
     * 进程内存占用率前5
     */
    String TOP5_PROCESS_MEM = "_TOP5_PROCESS_MEM:";
}
