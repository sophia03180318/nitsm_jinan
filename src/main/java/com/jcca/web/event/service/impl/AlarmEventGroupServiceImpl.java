package com.jcca.web.event.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.common.enums.AlarmStateEnum;
import com.jcca.web.alarm.dao.AlarmInfoMapper;
import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.event.dao.AlarmEventGroupMapper;
import com.jcca.web.event.entity.AlarmEventGroup;
import com.jcca.web.event.service.AlarmEventGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 告警事件配置
 *
 * @author lyp
 */
@Service
public class AlarmEventGroupServiceImpl extends ServiceImpl<AlarmEventGroupMapper, AlarmEventGroup>
        implements AlarmEventGroupService {

    @Resource
    private AlarmEventGroupMapper ruleGroupMapper;
    @Resource
    private AlarmInfoMapper alarmMapper;

    @Override
    public List<AlarmEventGroup> getAllByTypeId(String eventTypeId) {

        QueryWrapper<AlarmEventGroup> queryWrapper3 = new QueryWrapper<AlarmEventGroup>();
        queryWrapper3.like("EVENT_TYPE_IDS", eventTypeId);
        List<AlarmEventGroup> groupList = ruleGroupMapper.selectList(queryWrapper3);

        if (!groupList.isEmpty()) {

            List<AlarmEventGroup> groupLists = groupList.stream().filter(v -> {
                String[] split = v.getEventTypeIds().split(AlarmEventGroup.SPLIT_FLAG);
                List<String> asList = Arrays.asList(split);
                if (!asList.contains(eventTypeId)) {
                    return false;
                }
                return true;
            }).collect(Collectors.toList());

            return groupLists;
        }

        return groupList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeGroup(String id) throws Exception {
        // 相关告警恢复、删除告警组
        ruleGroupMapper.deleteById(id);

        QueryWrapper<AlarmInfo> queryWrapper = new QueryWrapper<AlarmInfo>();
        queryWrapper.eq("CORRELATION_ID", id);
        queryWrapper.eq("ALARM_STATE", AlarmStateEnum.ALARM.getCode());
        List<AlarmInfo> alarmList = alarmMapper.selectList(queryWrapper);

        for (AlarmInfo alarmInfo : alarmList) {
            alarmInfo.setStatus(AlarmStateEnum.RECOVER.getCode());
            alarmMapper.updateById(alarmInfo);
        }

    }

}
