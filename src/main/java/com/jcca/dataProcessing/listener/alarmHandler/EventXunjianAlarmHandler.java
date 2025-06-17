package com.jcca.dataProcessing.listener.alarmHandler;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.enums.StatusInfoChangeTypeEnum;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

import static com.jcca.web2.constant.Web2Const.XUNJIAN_COLLECT_QUEUE;

@Component("eventXunjianAlarmHandler")
public class EventXunjianAlarmHandler extends IFilterHandler<IEvent> {


    @Override
    public boolean handler(IEvent info) throws Exception {
                XUNJIAN_COLLECT_QUEUE.put(info);
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }
}
