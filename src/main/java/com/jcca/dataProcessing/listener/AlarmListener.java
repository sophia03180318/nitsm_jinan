package com.jcca.dataProcessing.listener;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @author Zhaozheng
 * @description TODO 告警监听用于告警信息的变动监听
 * @className AlarmListener
 * @date 2023/10/20 9:42
 * @since 2.1.0.0
 */
@Component("alarmListener")
public class AlarmListener implements IListener<IEvent> {
    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;
    @Override
    public void onEvent(IEvent event) {


        try {
            dataProcessManager.alarmInfoHandlerRequest(event);
        } catch (Exception e) {
            AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "rediskey~" + event.getEventRedisKey() + " mapkey~" + event.getMapKey() + " alarmInfoHandlerRequest 抛出异常", e);
        }

    }
}
