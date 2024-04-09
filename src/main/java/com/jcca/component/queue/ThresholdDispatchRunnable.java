package com.jcca.component.queue;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.redis.queue.RedisQueueTemplate;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.MyDateUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.dto.ReceiveCollectDto;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.component.thresholds.CollectHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * redis阈值数据处理调度
 */

@Slf4j
public class ThresholdDispatchRunnable implements Runnable {

    private static final int MAX_SLEEP_TIME = 5000;
    private static final int MIN_SLEEP_TIME = 800;
    private static final int STEP = 500;
    private static int SLEEP_TIME = 1000;

    private RedisQueueTemplate queueTemplate;
    private Integer pullSzie = 1000;
    private CollectHandler collectHandle;
    private ThreadPoolExecutor thresholdDisposePool;

    @Override
    public void run() {
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        collectHandle = SpringContextUtil.getBean(CollectHandler.class);
        thresholdDisposePool = (ThreadPoolExecutor) SpringContextUtil.getBean(ThreadPoolEnum.thresholdDataDisposePool);
        queueTemplate = new RedisQueueTemplate(stringRedisTemplate);
        while (true) {
            try {
                long starttime = System.currentTimeMillis();
                List<String> thresholdList = queueTemplate.lPop(RedisQueueConst.THRESHOLD_QUEUE, pullSzie);
                int size = controlSleepTime(thresholdList);
                if (CollUtil.isNotEmpty(thresholdList)) {
                    log.info("定时任务-本次拉取阈值队列[{}]设备数量:{}", RedisQueueConst.THRESHOLD_QUEUE, size);
                    CountDownLatch cdh = new CountDownLatch(thresholdList.size());
                    for (String bodyJson : thresholdList) {
                        if (StrUtil.isEmpty(bodyJson)) {
                            log.error("定时任务-redis推送阈值队列message空：{}", DateUtil.formatDateTime(new Date()));
                            cdh.countDown();
                            continue;
                        }
                        ReceiveCollectDto dto = JSONUtil.toBean(bodyJson, ReceiveCollectDto.class);

                        if (Objects.isNull(dto)) {
                            log.error("定时任务-redis推送阈值队列 -数据空req：{}", bodyJson);
                            cdh.countDown();
                            continue;
                        }

                        log.debug("定时任务-队列数据类型:{}报文【{}】", dto.getCategory(), bodyJson);

                        String content = dto.getContent();
                        if (!JSONUtil.isJsonArray(content)) {
                            log.error(AppLogUtils.logStr("采集队列：" + dto.getCategory(), "采集数据格式错误", content));
                            cdh.countDown();
                            continue;
                        }

                        JSONArray result = JSONUtil.parseArray(content);
                        thresholdDisposePool.execute(() -> {
                            try {
                                collectHandle.disposeData(dto.getCategory(), result);
                            } catch (Exception e) {
                                log.error("阈值队列处理错误,类型:{},参数:{}", dto.getCategory(), result, e);
                            } finally {
                                cdh.countDown();
                            }
                        });

                    }
                    cdh.await();
                    log.info(MyDateUtil.execTime("定时任务-本次阈值队列[" + RedisQueueConst.THRESHOLD_QUEUE + "]处理调度完成时间:", starttime));
                }
            } catch (InterruptedException e) {
                log.error("阈值处理[" + RedisQueueConst.THRESHOLD_QUEUE + "]调度被中断", e);
            } catch (Exception e) {
                log.error("阈值处理[" + RedisQueueConst.THRESHOLD_QUEUE + "]调度异常", e);
            } catch (Throwable e) {
                log.error("阈值处理[" + RedisQueueConst.THRESHOLD_QUEUE + "]调度崩溃", e);
            }

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                log.error("阈值处理[" + RedisQueueConst.THRESHOLD_QUEUE + "]调度被中断", e);
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
        int size = list.size();

        if (size < pullSzie && SLEEP_TIME < MAX_SLEEP_TIME) {
            SLEEP_TIME = SLEEP_TIME + STEP;
        } else if (size == pullSzie && SLEEP_TIME > MIN_SLEEP_TIME) {
            SLEEP_TIME = SLEEP_TIME - STEP;
        }
        return size;
    }

    public static void init() {
        Thread thresholdDispatchThread = new Thread(new ThresholdDispatchRunnable());
        thresholdDispatchThread.setName("thresholdDispatchThread-" + DateUtil.now());
        thresholdDispatchThread.start();

        if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "阈值队列处理调度初始化完成"));
        }
    }

}
