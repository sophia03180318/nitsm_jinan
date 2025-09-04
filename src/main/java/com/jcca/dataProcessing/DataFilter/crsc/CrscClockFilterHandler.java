package com.jcca.dataProcessing.DataFilter.crsc;


import com.jcca.dataProcessing.Entity.ItsmQueueEntity;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Zhaozheng
 * @description TODO 通号时间同步过滤处理类（暂时未作处理）
 * @className crscClockFilterHandler
 * @date 2023/10/27 9:27
 * @since 2.1.0.0
 */
@Component("crscClockFilterHandler")
public class CrscClockFilterHandler extends IFilterHandler<ItsmQueueEntity> {

    @Resource
    private IEventInfoManagerService eventInfoChangeManagerService;

    @Override
    public boolean handler(ItsmQueueEntity info) {

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }


}
