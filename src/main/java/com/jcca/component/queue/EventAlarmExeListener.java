package com.jcca.component.queue;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import com.jcca.common.input.ErrorCodeEnum;
import com.jcca.common.input.LogInputUtils;
import com.jcca.common.input.ServerTypeEnum;
import com.jcca.common.redis.queue.RedisQueueTemplate;
import com.jcca.common.utils.MyDateUtil;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.component.constants.RedisQueueConst;
import com.jcca.component.enums.ThreadPoolEnum;
import com.jcca.component.event.EventAlarmGroupLogicService;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 告警处理 必须顺序执行
 *
 * @author lyp
 */
@Slf4j
public class EventAlarmExeListener implements Runnable {

	private int pullSize = 1000;
	private int sleepTime = 2000;

	private ThreadPoolExecutor eventExecutor;

	private RedisQueueTemplate queueTemplate;
	private EventAlarmGroupLogicService ruleGroupService;

	@Override
	public void run() {

		eventExecutor = (ThreadPoolExecutor) SpringContextUtil.getBean(ThreadPoolEnum.alarmEventExe);
		queueTemplate = SpringContextUtil.getBean(RedisQueueTemplate.class);
		ruleGroupService = SpringContextUtil.getBean(EventAlarmGroupLogicService.class);

		while (true) {
			try {
				long starttime = System.currentTimeMillis();

				List<String> groupStrList = queueTemplate.lPop(RedisQueueConst.EVENT_GROUP_ALARM_EXE, pullSize);
				if (Objects.isNull(groupStrList)) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						if(LogInputUtils.inputError(ServerTypeEnum.QUEUE_EXE_ALARM_EVENT)){
							log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.QUEUE_EXE_ALARM_EVENT, ErrorCodeEnum.COMMON_QUEUE_INTERRUPTED_EXCEPTION,"",e.getMessage()),e);
						}
					}
					continue;
				}

				CountDownLatch cdh = new CountDownLatch(groupStrList.size());
				for (String queue : groupStrList) {
					eventExecutor.execute(() -> {
						try {
							ruleGroupService.exeAlarm(queue);
						} catch (Exception e) {
							if(LogInputUtils.inputError(ServerTypeEnum.QUEUE_EXE_ALARM_EVENT)){
								log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.QUEUE_EXE_ALARM_EVENT,ErrorCodeEnum.QUEUE_ALARM_EXE_ERROR,"",String.format("队列原始信息：%s ；异常信息：%s",queue, e.getMessage())));
							}
						} finally {
							cdh.countDown();
						}
					});

				}

				try {
					cdh.await();
				} catch (InterruptedException e) {
					if(LogInputUtils.inputError(ServerTypeEnum.QUEUE_EXE_ALARM_EVENT)){
						log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.QUEUE_EXE_ALARM_EVENT,ErrorCodeEnum.COMMON_QUEUE_INTERRUPTED_EXCEPTION,"",e.getMessage()));
					}
				}
				if(LogInputUtils.inputInfo(ServerTypeEnum.QUEUE_EXE_ALARM_EVENT)){
					log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.QUEUE_EXE_ALARM_EVENT,"",MyDateUtil.execTime("本次资产告警事件对列处理完成，用时：{}", starttime)));
				}

				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					if(LogInputUtils.inputError(ServerTypeEnum.QUEUE_EXE_ALARM_EVENT)){
						log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.QUEUE_EXE_ALARM_EVENT,ErrorCodeEnum.COMMON_QUEUE_INTERRUPTED_EXCEPTION,"",e.getMessage()),e);
					}
				}
			} catch (Exception e) {
				if(LogInputUtils.inputError(ServerTypeEnum.QUEUE_EXE_ALARM_EVENT)){
					log.error(LogInputUtils.formattingErrorLog(ServerTypeEnum.QUEUE_EXE_ALARM_EVENT,ErrorCodeEnum.QUEUE_ALARM_EXE_ERROR,"","事件告警规则队列出错"+e.getMessage()),e);
				}
			}
		}
	}

	public static void init() {
		Thread alarmPushRunnableThread = new Thread(new EventAlarmExeListener());
		alarmPushRunnableThread.setName("EventAlarmExeListenerThread-" + DateUtil.now());
		alarmPushRunnableThread.start();

		if(LogInputUtils.inputInfo(ServerTypeEnum.SYSTEM_INIT)){
			log.info(LogInputUtils.formattingInfoLog(ServerTypeEnum.SYSTEM_INIT,"","事件告警处理监听初始化完成"));
		}
	}

}
