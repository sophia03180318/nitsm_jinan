package com.jcca.dataProcessing.manager.alarmRepo;

import com.jcca.dataProcessing.Entity.EventAlarmLevelBaseEntity;

import java.util.List;

/**
 * @description: 告警规则监听管理类
 * @author: Lvyp
 * @create: 2023/12/23 16:58
 */
public interface AlarmRepoManager {


    /**
     * 变动
     */
    public void changeRepo();


    /**
     * 查询告警规则
     *
     * @param unicode
     * @return
     */
    public List<EventAlarmLevelBaseEntity> queryRepo(String unicode);


}
