package com.jcca.dataProcessing.listener.alarmHandler;

import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web2.constant.XunJianConst;
import org.springframework.stereotype.Component;

@Component("eventXunjianAlarmHandler")
public class EventXunjianAlarmHandler extends IFilterHandler<IEvent> {


    @Override
    public boolean handler(IEvent info) throws Exception {
        //巡检信息
        if (info.getInspectRecordId() != null && !"".equals(info.getInspectRecordId())) {
            XunJianConst.putXunJianCollectQueue(info.getInspectRecordId(), info);
        }
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
