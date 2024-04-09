package com.jcca.component.event.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 添加事件队列
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AddEventQueueBean extends CreateEventReq {

    /**
     * 事件命中的事件类型集合
     */
    private List<AddEventItem> typeList;

}
