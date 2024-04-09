package com.jcca.web.event.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.event.entity.AlarmEventGroup;

import java.util.List;

/**
 * 告警事件集合
 *
 * @author lyp
 */
public interface AlarmEventGroupService extends IService<AlarmEventGroup> {

    /**
     * 查询事件类型配置的所有告警规则组
     *
     * @param eventTypeId
     * @return
     */
    List<AlarmEventGroup> getAllByTypeId(String eventTypeId);

    /**
     * 删除
     *
     * @param id
     * @throws Exception
     */
    void removeGroup(String id) throws Exception;

}
