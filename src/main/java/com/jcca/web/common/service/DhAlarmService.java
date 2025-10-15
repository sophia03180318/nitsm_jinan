package com.jcca.web.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.common.entity.Alarm;
import com.jcca.web.common.entity.DhStation;

import java.util.List;

/**
 * @description: 组织
 * @author: sophia
 * @create: 2023/11/30 14:00
 **/
public interface DhAlarmService extends IService<Alarm> {
    List<Alarm> getAlarm();

    List<Alarm> getAssetAlarm();
}