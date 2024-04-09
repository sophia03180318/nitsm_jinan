package com.jcca.component.queue;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.redis.queue.RedisQueueTemplate;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.casco.BrokerAlarmHandler;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.dto.ItsmQueueReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author HanHW
 * @description 处理业务告警信息
 * @className BrokerAlarmRunnable
 * @date 2023/7/3 15:47
 * @since 2.0.5.0
 */
@Slf4j
public class BrokerAlarmRunnable implements Runnable {

    private static final int MAX_SLEEP_TIME = 5000;
    private static final int MIN_SLEEP_TIME = 1000;
    private static final int STEP = 500;
    private static int SLEEP_TIME = 1000;

    private int pullSzie = 1000;

    @Override
    public void run() {
        BrokerAlarmHandler brokerAlarmHandler = SpringContextUtil.getBean(BrokerAlarmHandler.class);
        StringRedisTemplate stringRedisTemplate = SpringContextUtil.getBean(StringRedisTemplate.class);
        RedisQueueTemplate queueTemplate = new RedisQueueTemplate(stringRedisTemplate);
        while (true) {
            List<String> list = queueTemplate.lPop(RedisQueueConst.BROKER_QUEUE_KEY, pullSzie);

            int size = controlSleepTime(list);

            if (CollUtil.isNotEmpty(list)) {
                if (LogInputUtils.inputInfo(ServerTypeEnum.QUEUE_BROKER)) {
                    log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.QUEUE_BROKER, "",
                            String.format("本次处理业务告警队列[" + RedisQueueConst.BROKER_QUEUE_KEY + "]数据：%s 条", size)));
                }

                for (String str : list) {
                    try {
                        ItsmQueueReq itsmQueueReq = JSONUtil.toBean(str, ItsmQueueReq.class);
                        brokerAlarmHandler.alarmHandle(itsmQueueReq.getCascoAlarmType(), itsmQueueReq);
                    } catch (Exception e) {
                        if (LogInputUtils.inputError(ServerTypeEnum.QUEUE_BROKER)) {
                            log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.QUEUE_BROKER, ErrorCodeEnum.QUEUE_BROKER_ERROR,
                                    "", String.format("处理业务告警数据时发生异常，原始数据：%s , 异常信息：%s", str, e.getMessage())), e);
                        }
                    }
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                if (LogInputUtils.inputError(ServerTypeEnum.QUEUE_BROKER)) {
                    log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.QUEUE_BROKER, ErrorCodeEnum.COMMON_QUEUE_INTERRUPTED_EXCEPTION, "", e.getMessage()), e);
                }
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
        Thread thread = new Thread(new BrokerAlarmRunnable());
        thread.setName("brokerAlarmThread");
        thread.start();

        if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "业务告警处理线程调度初始化完成"));
        }
    }
}
