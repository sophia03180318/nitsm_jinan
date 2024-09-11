package com.jcca.web.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.common.dao.DhAlarmMapper;
import com.jcca.web.common.entity.Alarm;
import com.jcca.web.common.service.DhAlarmService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @description:
 * @author: sophia
 * @create: 2024/09/11 16:56
 **/
@Service
public class DhAlarmServiceImpl extends ServiceImpl<DhAlarmMapper, Alarm> implements DhAlarmService {
    @Resource
    DhAlarmMapper alarmMapper;
    @Override
    public List<Alarm> getAlarm() {
        return alarmMapper.getAlarm();
    }
}