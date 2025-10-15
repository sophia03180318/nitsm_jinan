package com.jcca.dataProcessing.listener.eventInfoHandler;

import com.jcca.dataProcessing.Entity.EventAlarmLevelBaseEntity;
import com.jcca.dataProcessing.manager.impl.AlarmRepoManagerService;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.common.entity.Device;
import com.jcca.web.common.entity.DhFlag;
import com.jcca.web.common.service.DeviceService;
import com.jcca.web.event.enums.EventLevelEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 动环通过设备过滤告警
 *
 * @author sophia
 * @description TODO 动环通过设备过滤告警
 * @className EventIsFilterAlarmHandler
 * @date 2023/10/20 9:42
 * @since 2.1.0.0
 */
@Component("eventIsFilterAlarmHandler")
public class EventIsFilterAlarmHandler extends IFilterHandler<IEvent> {

    private static final String REPO_ID = "1743461406558396417";

    @Resource
    private DeviceService deviceService;

    @Override
    public boolean handler(IEvent info) {
        /**
         * 筛选动环设备
         */
        if (!Objects.isNull(info.getEventAlarmLevelBaseEntity()) && REPO_ID.equals(info.getEventAlarmLevelBaseEntity().getRepoId())) {
            EventAlarmLevelBaseEntity eventAlarmLevelBaseEntity = info.getEventAlarmLevelBaseEntity();
            String repoId = eventAlarmLevelBaseEntity.getRepoId();
            //调用方法 通过规则筛选设备
            DhFlag device = deviceService.isDevice(repoId, info.getAssetId());
            if (Objects.isNull(device)) {
                //没这个设备 就把告警规则踢出去
               eventAlarmLevelBaseEntity.setRepoId("1743461406558396417");
               eventAlarmLevelBaseEntity.setAlarmLevel(null);
            }
        }
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
