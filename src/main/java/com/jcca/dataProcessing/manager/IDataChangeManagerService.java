package com.jcca.dataProcessing.manager;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.support.IEvent;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.event.entity.AlarmEvent;

import java.util.Map;

/**
 * @author Zhaozheng
 * @description TODO
 * @className IalarmManagerService
 * @date 2023/10/20 10:26
 * @since 2.1.0.0
 */
public interface IDataChangeManagerService {
    /**
     * 保存事件信息
     *
     * @param info
     * @return
     */
    public AlarmEvent saveEvent(IEvent info);

    /**
     * 保存变动信息
     *
     * @param maps
     */
    public void saveInfo(Map<String, ChangeInfo> maps);

    /**
     * 弹告警
     *
     * @param asset
     * @param newAlarm
     */
    public void popup(Asset asset, AlarmInfo newAlarm);

    /**
     * 保存告警
     *
     * @param event
     * @param asset
     * @return
     */
    public AlarmInfo saveAlarm(IEvent event, Asset asset);

    /**
     * 告警关联事件
     *
     * @param alarmInfoId
     * @param alarmEvent
     */
    public void linkEvent(String alarmInfoId, AlarmEvent alarmEvent);
}
