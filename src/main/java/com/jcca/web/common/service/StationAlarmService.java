package com.jcca.web.common.service;


import com.jcca.web.common.controller.req.StationAlarmReqV2;
import com.jcca.web.common.controller.req.StationAlarmResp;

/**
 * @description: 车站告警处理
 * @author: Lvyp
 * @create: 2024/04/29 16:28
 */
public interface StationAlarmService {

    /**
     * 处理车站告警
     */
    StationAlarmResp disposeStationAlarm(StationAlarmReqV2 req);

}
