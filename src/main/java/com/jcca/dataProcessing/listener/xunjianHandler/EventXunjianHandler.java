package com.jcca.dataProcessing.listener.xunjianHandler;

import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.dataProcessing.support.XunjianEvent;
import org.springframework.stereotype.Component;

import static com.jcca.web2.constant.Web2Const.XUNJIAN_COLLECT_QUEUE;

@Component("eventXunjianHandler")
public class EventXunjianHandler extends IFilterHandler<IEvent> {

    @Override
    public boolean handler(IEvent info) throws  Exception {

        if(info.getInspectRecordId()!=null&&!"".equals(info.getInspectRecordId())){
            XUNJIAN_COLLECT_QUEUE.put(info);
        }


        return false;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return false;
    }
}
