package com.jcca.dataProcessing.manager.threshold;

import com.jcca.dataProcessing.manager.impl.ThresholdMangerService;
import com.jcca.dataProcessing.support.IListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


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

    @Override
    public void onEvent(Event event) {

        thresholdMangerService.changeThreshold();
    }
}
