package com.jcca.dataProcessing.listener.eventInfoHandler;

import com.jcca.dataProcessing.Entity.EventAlarmLevelBaseEntity;
import com.jcca.dataProcessing.manager.IEventInfoManagerService;
import com.jcca.dataProcessing.manager.impl.AlarmRepoManagerService;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判定事件是否上送告警，上送告警的级别
 * @author Zhaozheng
 * @description TODO 事件信息是否配置过告警处理类
 * @className EventIsConfigAlarmHandler
 * @date 2023/10/20 9:42
 * @since 2.1.0.0
 */
@Component("eventIsConfigAlarmHandler")
public class EventIsConfigAlarmHandler extends IFilterHandler<IEvent> {

    @Resource
    AlarmRepoManagerService alarmRepoManagerService;


    @Override
    public boolean handler(IEvent info) {

        /**
         * 如果事件配置过告警
         */
        List<EventAlarmLevelBaseEntity> list = alarmRepoManagerService.queryRepo(info.getEventRedisKey());
        if (list != null && list.size() > 0) {
            List<EventAlarmLevelBaseEntity> collect = list.stream().filter(item ->
                    info.getDescStr().contains(item.getStatusFlag().trim()) //事件描述信息中包含关键字信息
                            || EventLevelEnum.getMsgByCode(info.getStatus()).contains(item.getStatusFlag().trim()) //状态的中文code包含关键字信息
                            || info.getStatus().toString().contains(item.getStatusFlag().trim())).collect(Collectors.toList());//状态包含关键字信息
            if (collect != null && !collect.isEmpty()) {
                EventAlarmLevelBaseEntity eventAlarmLevelBaseEntity = collect.get(0);
                if(collect.size()>1){
                    //命中多个规则以最后创建的规则为准
                    List<EventAlarmLevelBaseEntity> collectDesc = collect.stream().sorted(Comparator.comparing(EventAlarmLevelBaseEntity::getCreateTime).reversed()).collect(Collectors.toList());
                    eventAlarmLevelBaseEntity = collectDesc.get(0);
                }
                info.setStatus(eventAlarmLevelBaseEntity.getFlagType());
                info.setEventAlarmLevelBaseEntity(eventAlarmLevelBaseEntity);
                this.dispatureEvent(info);
            }
        }


        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }

}
