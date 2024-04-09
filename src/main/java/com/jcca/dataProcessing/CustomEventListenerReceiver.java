package com.jcca.dataProcessing;

import com.jcca.dataProcessing.Entity.CustomEvent;
import com.jcca.dataProcessing.enums.CollectConst;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IAdapter;
import com.jcca.dataProcessing.support.IListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Zhaozheng
 * @description TODO
 * @className CustomEventListenerReceiver
 * @date 2023/12/19 15:23
 * @since 2.1.0.0
 */
@Component("customEventListenerReceiver")
public class CustomEventListenerReceiver implements IListener<CustomEvent> {
    @Resource
    private DataProcessManager dataProcessManager;

    @Override
    public void onEvent(CustomEvent event) {
        IAdapter adapter = dataProcessManager.getAdapter(CollectConst.CUSTOMEVENT);
        adapter.dispose(event);
    }
}
