package com.jcca.component.enums;

public class ThreadPoolEnum {
    // 阈值采集数据处理线程池
    public static final String thresholdDataDisposePool = "thresholdDataDisposePool";
    // 非阈值类型告警处理线程池
    public static final String noThresholdAlarmDispose = "noThresholdAlarmDispose";
    // 卡斯科告警处理线程池
    public static final String cascoAlarmDispose = "cascoAlarmDispose";
    // 非核心业务后台线程池
    public static final String taskExecutor = "taskExecutor";
    //智能巡检
    public static final String xunjianExecutor = "xunjianExecutor";
    // 事件告警处理线程
    public static final String alarmEventExe = "alarmEventExe";
    /**
     * 添加事件处理线程
     */
    public static final String alarmEventAdd = "alarmEventAdd";
    public static final String linksNumExecutor = "linksNumExecutor";
    public static final String transferDataExecutor = "transferDataExecutor";
    public static final String ipmiPingJob = "ipmiPingJob";
    public static final String xunjianAsync = "xunjianAsync";

    public static final String XUNJIAN_FIANL = "xunjianFianl";
}
