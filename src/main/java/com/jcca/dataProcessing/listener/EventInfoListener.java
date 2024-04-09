package com.jcca.dataProcessing.listener;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author Zhaozheng
 * @description TODO 事件监听器，用于监听事件的变动
 * @className EventInfoListener
 * @date 2023/10/20 9:42
 * @since 2.1.0.0
 */
@Component("eventInfoListener")
public class EventInfoListener implements IListener<IEvent> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;


    ThreadPoolExecutor excutorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void onEvent(IEvent event) {
        if (Objects.isNull(dataProcessManager)) {
            dataProcessManager = SpringContextUtil.getBean(DataProcessManager.class);
        }
        try {
            dataProcessManager.evntInfoHandlerRequest(event);
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "rediskey~" + event.getRedisKey() + " mapkey~" + event.getMapKey() + " alarmInfoHandlerRequest 抛出异常", e);
        }
    }
}
