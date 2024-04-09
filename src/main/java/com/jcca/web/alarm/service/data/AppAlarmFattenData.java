package com.jcca.web.alarm.service.data;

import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.event.entity.AlarmEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


/**
 * 告警信息
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppAlarmFattenData extends AlarmInfo {

    private static final long serialVersionUID = 1L;

    private List<AlarmEvent> eventList;

}
