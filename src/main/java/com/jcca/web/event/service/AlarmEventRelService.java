package com.jcca.web.event.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.event.entity.AlarmEventRel;

import java.util.List;

/**
 * 告警事件关联表
 *
 * @author lyp
 */
public interface AlarmEventRelService extends IService<AlarmEventRel> {


    /**
     * 查询告警下关联的事件
     *
     * @param id
     * @return
     */
    List<AlarmEventRel> listByAlarmId(String id);

    /**
     * 查询事件ID列表
     *
     * @param id
     * @return
     */
    List<String> getAllEventIdByAlarmId(String id);

}
