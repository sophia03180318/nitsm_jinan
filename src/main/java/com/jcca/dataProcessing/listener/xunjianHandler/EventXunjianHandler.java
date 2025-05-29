package com.jcca.dataProcessing.listener.xunjianHandler;

import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import static com.jcca.web2.constant.Web2Const.XUNJIAN_COLLECT_QUEUE;

@Component("eventXunjianHandler")
public class EventXunjianHandler extends IFilterHandler<IEvent> {
    @Override
    public boolean handler(IEvent info) throws ResultException, Exception {
        XUNJIAN_COLLECT_QUEUE.put(info);
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }
}
