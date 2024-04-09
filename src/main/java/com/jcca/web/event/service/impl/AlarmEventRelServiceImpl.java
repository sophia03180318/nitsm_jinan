package com.jcca.web.event.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.event.dao.AlarmEventRelMapper;
import com.jcca.web.event.entity.AlarmEventRel;
import com.jcca.web.event.service.AlarmEventRelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 告警事件关联
 *
 * @author lyp
 */
@Service
public class AlarmEventRelServiceImpl extends ServiceImpl<AlarmEventRelMapper, AlarmEventRel>
        implements AlarmEventRelService {

    @Resource
    private AlarmEventRelMapper relMapper;

    @Override
    public List<AlarmEventRel> listByAlarmId(String id) {
        if (StrUtil.isEmpty(id)) {
            return new ArrayList<AlarmEventRel>();
        }

        return relMapper.selectByAlarmId(id);
    }

    @Override
    public List<String> getAllEventIdByAlarmId(String id) {
        List<String> eventId = relMapper.selectEventIdByAlarmId(id);
        if (Objects.isNull(eventId)) {
            eventId = new ArrayList<String>();
        }
        return eventId;
    }

}
