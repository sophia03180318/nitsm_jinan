package com.jcca.dataProcessing.manager.alarmRepo;

import com.jcca.dataProcessing.manager.impl.AlarmRepoManagerService;
import com.jcca.dataProcessing.support.IListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 告警规则监听器
 *
 * @author Zhaozheng
 * @description TODO
 * @className AlarmRepoListener
 * @date 2023/12/23 17:19
 * @since 2.1.0.0
 */
@Component("alarmRepoListener")
public class AlarmRepoListener implements IListener<AlarmRepoEvent> {
    @Resource
    private AlarmRepoManagerService alarmRepoManagerService;

    @Override
    public void onEvent(AlarmRepoEvent event) {
        alarmRepoManagerService.changeRepo();

    }
}
