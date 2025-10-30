package com.jcca.dataProcessing.listener.eventInfoHandler;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

/**
 * 过滤车站数据
 * 车站数据只通过推送方式上送告警
 */
@Component("eventFilterStationDataHandler")
public class EventFilterStationDataHandler  extends IFilterHandler<IEvent> {


    @Override
    public boolean handler(IEvent info) throws ResultException, Exception {
        return StrUtil.isEmpty(info.getVersion());
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
