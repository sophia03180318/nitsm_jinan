package com.jcca.web.event.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.component.event.bean.AddEventItem;
import com.jcca.web.event.entity.AlarmEventType;

import java.util.List;

/**
 * 告警事件类型
 *
 * @author lyp
 */
public interface AlarmEventTypeService extends IService<AlarmEventType> {

    /**
     * 通过匹配码和原始信息
     * 查询命中的所有事件类型
     *
     * @param originalMsg
     * @return
     */
    List<AddEventItem> getAllEventTypeByMsg(String uniqueCode, String originalMsg);

    /**
     * 查询系统中的未知事件，没有的话会新建
     *
     * @return
     */
    AlarmEventType queryUnkonwEvent();

}
