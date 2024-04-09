package com.jcca.web.event.controller.bean;

import com.jcca.web.event.entity.AlarmEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页查询响应信息
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventTypeQueryResp extends AlarmEventType {

    private static final long serialVersionUID = 1L;

}
