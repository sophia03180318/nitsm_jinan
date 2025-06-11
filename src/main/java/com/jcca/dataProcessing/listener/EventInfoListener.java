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
import java.util.concurrent.*;


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



        Future<Integer> future=excutorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                if (Objects.isNull(dataProcessManager)) {
                    dataProcessManager = SpringContextUtil.getBean(DataProcessManager.class);
                }
                try {
                    dataProcessManager.evntInfoHandlerRequest(event);
                } catch (Exception e) {
                    AppLogUtils.buildLogError(LogFunctionEnum.DATA_PROCESS, "rediskey~" + event.getEventRedisKey() + " mapkey~" + event.getMapKey() + " alarmInfoHandlerRequest 抛出异常", e);
                }
                return 1;
            }
        });

        if(event.getInspectRecordId()!=null&&!"".equals(event.getInspectRecordId())){
            try {
                future.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }





    }
}
