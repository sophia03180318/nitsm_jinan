package com.jcca.web2.constant;

import com.jcca.dataProcessing.support.IEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author lifp
 * @version 1.0
 * @description: 智能巡检 - 常量
 * @date 2025-12-05 星期五 13:47:30
 */
@Slf4j
public final class XunJianConst {
    // 当前指标结束标识
    public static final int FINISH_FLAG = 9999;
    // 巡检超时时间 5分钟
    public static final int XUNJIAN_TIME_OUT = 300 * 1000;
    // 巡检数据采集接口
    public static final String XUNJIAN_CENTER_URI = "/business/exeCollect";
    // 巡检状态数据接口
    public static final String XUNJIAN_PROCESS_URI = "/business/exeProcessStatusPush";

    // 当前巡检资产
    public static final Map<String, String> currentAssetIdMap = new ConcurrentHashMap<>();

    // 任务容器：用于重置任务状态 <jobId, recordId>
    public static final Map<String, String> XUNJIAN_JOB_RECORD = new ConcurrentHashMap<>();

    // 用于停止任务
    public static final Map<String, Thread> INSPECT_THREAD_MAP = new ConcurrentHashMap<>();

    // 巡检实时采集队列
    public static final Map<String, LinkedBlockingQueue<IEvent>> XUNJIAN_COLLECT_QUEUE = new HashMap<>();

    /**
     * 初始化指定 inspectId 的采集队列（可指定容量）
     * 建议在巡检任务开始前调用
     */
    public static synchronized void initXunJianCollectQueue(String inspectId, int capacity) {
        if (inspectId == null || capacity <= 0) {
            return;
        }
        if (!XUNJIAN_COLLECT_QUEUE.containsKey(inspectId)) {
            XUNJIAN_COLLECT_QUEUE.put(inspectId, new LinkedBlockingQueue<>(capacity));
        }
    }

    /**
     * 初始化默认容量（例如 1000）的队列
     */
    public static synchronized void initXunJianCollectQueue(String inspectId) {
        initXunJianCollectQueue(inspectId, 1000);
    }

    /**
     * 向已存在的巡检采集队列中添加事件
     * 注意：仅当队列已通过 init 方法初始化时才生效
     *
     * @param inspectId 巡检ID
     * @param iEvent    事件对象
     * @return true 表示成功加入队列，false 表示队列不存在或入队失败
     */
    public static boolean putXunJianCollectQueue(String inspectId, IEvent iEvent) { // 修正拼写：Queur → Queue
        if (inspectId == null || iEvent == null) {
            return false;
        }

        LinkedBlockingQueue<IEvent> queue = XUNJIAN_COLLECT_QUEUE.get(inspectId);
        if (queue == null) {
            log.warn("队列未初始化: {}", inspectId);
            return false;
        }

        // 使用 offer() 避免队列满时抛异常
        return queue.offer(iEvent);
    }

    /**
     * 判断指定的巡检ID是否已初始化采集队列
     *
     * @param inspectId 巡检任务ID
     * @return true 表示已存在对应队列，false 表示未初始化或为 null
     */
    public static boolean hasXunJianCollectQueue(String inspectId) {
        if (inspectId == null) {
            return false;
        }
        return XUNJIAN_COLLECT_QUEUE.containsKey(inspectId);
    }
}
