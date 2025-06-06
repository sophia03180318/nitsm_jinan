package com.jcca.web2.constant;

import com.jcca.dataProcessing.support.IEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author HanHW
 * @description 所有常量存放处
 * @className Web2Const
 * @date 2023/10/26 18:27
 * @since 2.1.0.0
 */
public interface Web2Const {

    // 日志扫描位置
    String PACKAGE_NAME = "com.jcca";
    // 功能日志缓存 <code, status>
    Map<String, Integer> FUNCTION_LOG_MAP = new HashMap<>(128);


    // 推送数据常量
    String STATISTICS_RIGHT_DOWN = "1"; // 大屏右下角滚动消息
    /**
     * 告警消息
     */
    String STATISTICS_TOP_MSG_ALARM = "2";
    String STATISTICS_TOP_MSG_HANDLE = "3"; // 大屏上方滚动处理消息
    String ALARM_RIGHT_DOWN = "4"; // 原页面右下角告警消息

    // 巡检指标状态 0未知，1待巡检，2正在巡检，3巡检正常，4巡检异常
    String UNKNOWN = "0";
    String INSPECT = "1";
    String INSPECTING = "2";
    String INSPECTED = "3";
    String INSPECT_ERROR = "4";

    // 巡检管理操作状态
    String INSPECT_NO = "NO"; // 未开始
    String INSPECT_BEGIN = "BEGIN"; // 开始
    String INSPECT_PAUSE = "PAUSE"; // 暂停
    String INSPECT_END = "END"; // 结束

    // 巡检类型 1按设备巡检，2按指标巡检
    String INSPECT_ASSET = "1";
    String INSPECT_TARGET = "2";

    // 虚拟机柜ID，存放不在机柜内的设备
    String VIR_CABINET_ID = "0";
    String VIR_CABINET_NAME = "终端";

    // 资产阈值启用状态 0不可用，1可用
    Integer UNAVAILABLE = 0;
    Integer AVAILABLE = 1;

    // 维护计划天窗时间缓存前缀
    String MAINTENANCE_FIX = "maintenance:";
    // 采集指标表达式 时间间隔
    String TARGET_INTERVAL = "INTERVAL";

    // 巡检实时采集队列
    LinkedBlockingQueue<IEvent> XUNJIAN_COLLECT_QUEUE = new LinkedBlockingQueue<>(20000);

    // 巡检数据采集接口
    String XUNJIAN_CENTER_URI = "/business/exeCollect";
    // 巡检状态数据接口
    String XUNJIAN_PROCESS_URI = "/business/exeProcessStatusPush";
    // 用于重置任务状态 <jobId, recordId>
    Map<String, String> XUNJIAN_JOB_RECORD = new ConcurrentHashMap<>();
    // 巡检需要查询的指标
    String[] STATUS_TARGET_ARR = {"event:event_CPU:status", "event:event_process:status",
            "event:event_port:optical_state", "event:event_port:state", "event:event_process:once",
            "event:event_fan:state", "event:event_fan:unknown", "event:event_power:syslog", "event:event_power:state", "event:tableSpace", "event:event_db:connect",
            "event:event_temp:state", "event:event_temp:normal", "event:event_led:state", "event:event_time:run_restart", "event:event_time:clock_state"};

}
