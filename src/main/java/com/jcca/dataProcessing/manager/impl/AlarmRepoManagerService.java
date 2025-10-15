package com.jcca.dataProcessing.manager.impl;

import cn.hutool.core.util.StrUtil;
import com.jcca.dataProcessing.Entity.EventAlarmLevelBaseEntity;
import com.jcca.dataProcessing.manager.alarmRepo.AlarmRepoManager;
import com.jcca.web.alarm.entity.AlarmRepository;
import com.jcca.web.alarm.service.AlarmRepositoryService;
import com.jcca.web.event.entity.AlarmEventType;
import com.jcca.web.event.service.AlarmEventTypeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 告警规则
 * @author: Lvyp
 * @create: 2023/12/23 16:57
 */
@Service
public class AlarmRepoManagerService implements AlarmRepoManager {

    /**
     * 缓存的事件配置信息
     */
    private Map<String, List<EventAlarmLevelBaseEntity>> eventLevels = new HashMap<>();

    @Resource
    private AlarmRepositoryService repoServ;
    @Resource
    private AlarmEventTypeService eventTypeServ;

    /**
     * 初始化所有的知识库
     */
    public void init() {
        List<AlarmRepository> repoList = repoServ.list();
        Map<String, List<AlarmRepository>> collect = repoList.stream().collect(Collectors.groupingBy(AlarmRepository::getAlarmCode));
        Set<String> keySet = collect.keySet();
        for (String key : keySet) {
            List<EventAlarmLevelBaseEntity> mapList = new ArrayList<>();
            List<AlarmRepository> groupList = collect.get(key);
            for (AlarmRepository alarmRepository : groupList) {
                EventAlarmLevelBaseEntity entity = new EventAlarmLevelBaseEntity();
                entity.setAlarmLevel(alarmRepository.getAlarmLevel());
                entity.setEventTypeId(alarmRepository.getEventTypeId());
                entity.setEventTypeName("未配制类型的告警");
                if(StrUtil.isNotEmpty(alarmRepository.getTemplateStr())){
                    entity.setTemplateStr(alarmRepository.getTemplateStr());
                }
                if (StrUtil.isNotEmpty(alarmRepository.getEventTypeId())) {
                    AlarmEventType type = eventTypeServ.getById(alarmRepository.getEventTypeId());
                    if (Objects.nonNull(type)) {
                        entity.setEventTypeName(type.getName());
                    }
                    entity.setRepoId(alarmRepository.getId());
                }
                entity.setOpinion(alarmRepository.getPlanStr());
                entity.setFlagType(alarmRepository.getFlagType());
                entity.setStatusFlag(alarmRepository.getStatusFlag());
                entity.setAlarmCode(alarmRepository.getAlarmCode());
                entity.setCreateTime(alarmRepository.getCreateTime());
                mapList.add(entity);
            }
            eventLevels.put(key, mapList);
        }
    }

    @Override
    public void changeRepo() {
        eventLevels.clear();
        init();
    }

    @Override
    public List<EventAlarmLevelBaseEntity> queryRepo(String unicode) {
        return eventLevels.get(unicode);
    }
}
