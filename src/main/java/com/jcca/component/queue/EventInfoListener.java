package com.jcca.component.queue;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.redis.queue.RedisQueueTemplate;
import com.jcca.common.utils.MyDateUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.component.event.EventLogicService;
import com.jcca.component.event.bean.AddEventQueueBean;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 添加事件监听
 *
 * @author lyp
 */
@Slf4j
public class EventInfoListener implements Runnable {

    private int pullSize = 1000;
    private int sleepTime = 1000;

    private ThreadPoolExecutor eventExecutor;
    private RedisQueueTemplate queueTemplate;
    private EventLogicService logicServ;

    @Override
    public void run() {
        eventExecutor = (ThreadPoolExecutor) SpringContextUtil.getBean(ThreadPoolEnum.alarmEventAdd);
        queueTemplate = SpringContextUtil.getBean(RedisQueueTemplate.class);
        logicServ = SpringContextUtil.getBean(EventLogicService.class);

        while (true) {
            try {
                long starttime = System.currentTimeMillis();
                List<String> groupStrList = queueTemplate.lPop(RedisQueueConst.EVENT_GROUP_ALARM_ADD, pullSize);

                if (Objects.isNull(groupStrList) || groupStrList.isEmpty()) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                    continue;
                }

                CountDownLatch cdh = new CountDownLatch(groupStrList.size());

                for (String enevtStr : groupStrList) {
                    eventExecutor.execute(() -> {
                        try {
                            AddEventQueueBean req = JSONUtil.toBean(JSONUtil.parseObj(enevtStr),
                                    AddEventQueueBean.class);
                            logicServ.addEventQueue(req);
                        } catch (Exception e) {
                            if (LogInputUtils.inputError(ServerTypeEnum.QUEUE_EVENT_ADD)) {
                                log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.QUEUE_EVENT_ADD, ErrorCodeEnum.QUEUE_EVENT_ADD_ERROR, "", String.format("原始报文：%s,异常信息：%s", enevtStr, e.getMessage())), e);
                            }
                        } finally {
                            cdh.countDown();
                        }
                    });
                }

                try {
                    cdh.await();
                } catch (InterruptedException e) {
                    if (LogInputUtils.inputError(ServerTypeEnum.QUEUE_EVENT_ADD)) {
                        log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.QUEUE_EVENT_ADD, ErrorCodeEnum.COMMON_QUEUE_INTERRUPTED_EXCEPTION, "", e.getMessage()), e);
                    }
                }

                if (LogInputUtils.inputInfo(ServerTypeEnum.QUEUE_EVENT_ADD)) {
                    log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.QUEUE_EVENT_ADD, "", MyDateUtil.execTime("事件添加--队列处理完成", starttime)));
                }

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    if (LogInputUtils.inputError(ServerTypeEnum.QUEUE_EVENT_ADD)) {
                        log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.QUEUE_EVENT_ADD, ErrorCodeEnum.COMMON_QUEUE_INTERRUPTED_EXCEPTION, "", e.getMessage()), e);
                    }
                }
            } catch (Exception e) {
                if (LogInputUtils.inputError(ServerTypeEnum.QUEUE_EVENT_ADD)) {
                    log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.QUEUE_EVENT_ADD, ErrorCodeEnum.QUEUE_EVENT_ADD_ERROR_02, "", e.getMessage()), e);
                }
            }
        }

    }

    /**
     * 初始化
     */
    public static void init() {
        Thread alarmPushRunnableThread = new Thread(new EventInfoListener());
        alarmPushRunnableThread.setName("EventAlarmAddListenerThread-" + DateUtil.now());
        alarmPushRunnableThread.start();
        if (LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)) {
            log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT, "", "事件添加处理监听初始化完成"));
        }
    }

}
