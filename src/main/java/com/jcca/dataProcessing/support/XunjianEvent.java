package com.jcca.dataProcessing.support;

import com.jcca.dataProcessing.Entity.ChangeInfo;
import com.jcca.dataProcessing.Entity.CommonEntity;
import com.jcca.dataProcessing.Entity.EventAlarmLevelBaseEntity;
import com.jcca.dataProcessing.manager.bean.AlarmTempReq;
import com.jcca.web.event.enums.EventLevelEnum;
import lombok.Data;

import java.util.Date;


/**
 * 事件类，向后传递的事件信息
 */
@Data
public class XunjianEvent {
    CommonEntity info;


}
