package com.jcca.dataProcessing.listener;

import com.jcca.common.log.enums.LogFunctionEnum;
import com.jcca.common.utils.AppLogUtils;
import com.jcca.common.utils.SpringContextUtil;
import com.jcca.dataProcessing.manager.DataProcessManager;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IListener;
import com.jcca.dataProcessing.support.XunjianEvent;
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
@Component("xunjianEventInfoListener")
public class XunjianEventInfoListener implements IListener<XunjianEvent> {

    @Resource(name = "dataProcessManager")
    private DataProcessManager dataProcessManager;


    @Override
    public void onEvent(XunjianEvent event) throws Exception {
        dataProcessManager.xunjianInfoHandlerRequest(event);

    }
}
