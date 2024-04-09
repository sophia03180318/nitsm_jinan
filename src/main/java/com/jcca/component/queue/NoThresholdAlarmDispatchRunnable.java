package com.jcca.component.queue;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.redis.queue.RedisQueueTemplate;
import com.jcca.common.utils.MyDateUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.dto.ReceiveAlarmDto;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.component.other.AlarmHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * redis非阈值告警队列处理调度
 */

@Slf4j
public class NoThresholdAlarmDispatchRunnable implements Runnable {

    private static final int MAX_SLEEP_TIME = 3000;
    private static final int MIN_SLEEP_TIME = 1300;
    private static final int STEP = 300;
    private static int SLEEP_TIME = 1000;

    private RedisQueueTemplate queueTemplate;
    private Integer pullSzie = 1000;
    private ThreadPoolExecutor noThresholdAlarmDispose;
    private AlarmHandler alarmHandler;

    @Override
    public void run() {
        alarmHandler = SpringContextUtil.getBean(AlarmHandler.class);
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        noThresholdAlarmDispose = (ThreadPoolExecutor) SpringContextUtil
                .getBean(ThreadPoolEnum.noThresholdAlarmDispose);
        queueTemplate = new RedisQueueTemplate(stringRedisTemplate);
        while (true) {
            try {
                long starttime = System.currentTimeMillis();
                List<String> noThresholdAlarmList = queueTemplate.lPop(RedisQueueConst.ALARM_QUEUE, pullSzie);

                int size = controlSleepTime(noThresholdAlarmList);

                if (CollUtil.isNotEmpty(noThresholdAlarmList)) {
                    log.info("定时任务-本次非阈值告警队列[" + RedisQueueConst.ALARM_QUEUE + "]拉取数据量:{}", size);
                    CountDownLatch cdh = new CountDownLatch(noThresholdAlarmList.size());
                    for (String bodyJson : noThresholdAlarmList) {
                        if (StrUtil.isEmpty(bodyJson)) {
                            log.error("定时任务-redis推送告警队列message空：{}", DateUtil.formatDateTime(new Date()));
                            cdh.countDown();
                            continue;
                        }

                        ReceiveAlarmDto alarmDto = JSONUtil.toBean(bodyJson, ReceiveAlarmDto.class);
                        noThresholdAlarmDispose.execute(() -> {
                            try {
                                alarmHandler.alarmHandle(alarmDto.getCategory(), alarmDto);
                            } catch (Exception e) {
                                log.error("定时任务-非阈值告警队列[" + RedisQueueConst.ALARM_QUEUE + "]处理错误:{}", e.getMessage(), e);
                            } finally {
                                cdh.countDown();
                            }
                        });

                    }

                    cdh.await();
                    log.info(MyDateUtil.execTime("定时任务-本次非阈值告警队列处理完成时间", starttime));

                }
            } catch (Exception e) {
                log.error("定时任务-非阈值告警队列[" + RedisQueueConst.ALARM_QUEUE + "]处理异常", e);
            } catch (Throwable e) {
                log.error("定时任务-非阈值告警队列[" + RedisQueueConst.ALARM_QUEUE + "]处理崩溃", e);
            }

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                log.error("定时任务-非阈值告警队列[" + RedisQueueConst.ALARM_QUEUE + "]被中断", e);
            }
        }
    }

    /**
     * 调整线程睡眠时长
     *
     * @param list
     * @return
     */
    private int controlSleepTime(List<String> list) {
        int size = Objects.isNull(list.size()) ? 0 : list.size();

        if (size < pullSzie && SLEEP_TIME < MAX_SLEEP_TIME) {
            SLEEP_TIME = SLEEP_TIME + STEP;
        } else if (size == pullSzie && SLEEP_TIME > MIN_SLEEP_TIME) {
            SLEEP_TIME = SLEEP_TIME - STEP;
        }
        return size;
    }

    public static void init() {
        Thread alarmDispatchRunnable = new Thread(new NoThresholdAlarmDispatchRunnable());
        alarmDispatchRunnable.setName("noThresholdAlarmDispatchRunnable-" + DateUtil.now());
        alarmDispatchRunnable.start();
        if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "定时任务-非阈值队列处理调度初始化完成"));
        }
    }

}
