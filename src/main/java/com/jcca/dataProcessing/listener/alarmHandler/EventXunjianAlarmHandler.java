package com.jcca.dataProcessing.listener.alarmHandler;

import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import static com.jcca.web2.constant.Web2Const.XUNJIAN_COLLECT_QUEUE;

@Component("eventXunjianAlarmHandler")
public class EventXunjianAlarmHandler extends IFilterHandler<IEvent> {


    @Override
    public boolean handler(IEvent info) throws Exception {
        //巡检信息
        if (info.getInspectRecordId() != null && !"".equals(info.getInspectRecordId())) {
            XUNJIAN_COLLECT_QUEUE.put(info);
        }
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
