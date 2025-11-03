package com.jcca.dataProcessing.listener.eventInfoHandler;

import com.jcca.common.enums.AlarmLevelEnum;
import com.jcca.dataProcessing.manager.impl.AlarmRepoManagerService;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 触发告警流程
 *
 * @author sophia
 * @description TODO 触发告警流程
 * @className EventIsConfigAlarmHandler
 * @date 2023/10/20 9:42
 * @since 2.1.0.0
 */
@Component("eventIsPushAlarmHandler")
public class EventIsPushAlarmHandler extends IFilterHandler<IEvent> {

    @Resource
    AlarmRepoManagerService alarmRepoManagerService;


    @Override
    public boolean handler(IEvent info) {
        /**
         * 如果事件携带告警配置规则
         */
        if (!Objects.isNull(info.getEventAlarmLevelBaseEntity())) {
            if(!AlarmLevelEnum.UN_CONFIG.getCode().equals(info.getEventAlarmLevelBaseEntity().getAlarmLevel())){
                this.dispatureEvent(info);
                return false;
            }
        }



        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
