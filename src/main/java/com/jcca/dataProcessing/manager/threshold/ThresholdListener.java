package com.jcca.dataProcessing.manager.threshold;

import com.jcca.dataProcessing.manager.impl.ThresholdMangerService;
import com.jcca.dataProcessing.support.IListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author Zhaozheng
 * @description TODO 阈值事件监听器
 * @className AlarmListener
 * @date 2023/10/20 9:42
 * @since 2.1.0.0
 */
@Component("thresholdListener")
public class ThresholdListener implements IListener<Event> {
    @Resource
    private ThresholdMangerService thresholdMangerService;


    ThreadPoolExecutor excutorService=new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void onEvent(Event event) {
        excutorService.submit(() -> thresholdMangerService.changeThreshold());

    }
}
