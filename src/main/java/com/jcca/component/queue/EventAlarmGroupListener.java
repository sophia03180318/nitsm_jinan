package com.jcca.component.queue;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.redis.queue.RedisQueueTemplate;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.event.EventAlarmGroupLogicService;
import com.jcca.component.event.bean.EventAlarmGroupQueueReq;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * 预处理告警，分析告警队列中的数据 必须顺序执行
 *
 * @author lyp
 */
@Slf4j
public class EventAlarmGroupListener implements Runnable {

    private int pullSize = 1000;
    private int sleepTime = 2000;
    private int maxSleepTime = 5000;
    private int minSleepTime = 800;
    //记录处理条数
    public static Long runSize = 0L;

    private RedisQueueTemplate queueTemplate;
    private EventAlarmGroupLogicService ruleGroupService;

    @Override
    public void run() {
        queueTemplate = SpringContextUtil.getBean(RedisQueueTemplate.class);
        ruleGroupService = SpringContextUtil.getBean(EventAlarmGroupLogicService.class);

        while (true) {
            try {
                List<String> groupStrList = queueTemplate.lPop(RedisQueueConst.EVENT_GROUP_ALARM, pullSize);

                if (Objects.isNull(groupStrList) || groupStrList.isEmpty()) {
                    try {
                        sleepTime = maxSleepTime;
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        log.error("定时任务-事件告警规则队列[" + RedisQueueConst.EVENT_GROUP_ALARM + "]异常", e);
                    }

                    continue;
                }

                for (String queueStr : groupStrList) {
                    if (runSize == Long.MAX_VALUE) {
                        runSize = 0L;
                    }
                    runSize = runSize + 1;

                    EventAlarmGroupQueueReq group = JSONUtil.toBean(JSONUtil.parseObj(queueStr),
                            EventAlarmGroupQueueReq.class);
                    ruleGroupService.parseGroup(group);
                }

                if (groupStrList.size() < pullSize && sleepTime < maxSleepTime) {
                    // 处理速度过快 延长睡眠100毫秒 为系统其他线程节省事件
                    sleepTime += 100;
                }

                if (groupStrList.size() == pullSize && sleepTime > minSleepTime) {
                    // 处理速度过慢 减少睡眠时常
                    sleepTime -= 100;
                }

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    log.error("定时任务-事件组处理监听队列[" + RedisQueueConst.EVENT_GROUP_ALARM + "]被中断", e);
                }
            } catch (Exception e) {
                log.error("定时任务-分发队列[" + RedisQueueConst.EVENT_GROUP_ALARM + "]出错：{}", e.getMessage(), e);
            }
        }
    }

    public static void init() {
        Thread alarmPushRunnableThread = new Thread(new EventAlarmGroupListener());
        alarmPushRunnableThread.setName("EventAlarmGroupListenerThread-" + DateUtil.now());
        alarmPushRunnableThread.start();

        if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "事件组处理监听初始化完成"));
        }
    }

}
