package com.jcca.dataProcessing.listener.eventInfoHandler;

import com.jcca.common.enums.AlarmLevelEnum;
import com.jcca.dataProcessing.manager.impl.AlarmRepoManagerService;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web2.service.AlarmWhitelistService;
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
    @Resource
    private AlarmWhitelistService alarmWhitelistService;


    @Override
    public boolean handler(IEvent info) {
        String alarmCoded = info.getEventRedisKey();
        String flag = info.getMapKey();

        //如果事件携带告警配置规则
        if (Objects.isNull(info.getEventAlarmLevelBaseEntity())) {
            return true;
        }
        // 过滤未配置
        if(AlarmLevelEnum.UN_CONFIG.getCode().equals(info.getEventAlarmLevelBaseEntity().getAlarmLevel())){
            return true;
        }

        int batchList = alarmWhitelistService.queryWhiteCount(flag, alarmCoded, info.getAssetId());
        //屏蔽清单过滤
        if(batchList != 0){
            return true;
        }

        this.dispatureEvent(info);
        return false;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
