package com.jcca.web2.dto.xunjian;

import com.jcca.web.alarm.entity.AlarmInfo;
import lombok.Data;

import java.util.List;

/**
 * @author: hhw
 * @description: InspectTargetDetailInfoVo 主要是用来
 * @date: 2025-05-23  11:53
 * @since: 2.1.6.0
 */
@Data
public class InspectTargetDetailInfoVo {

    private List<InspectTargetDetailInfo> targetDetailList;

    private List<AlarmInfo> alarmInfoList;
}
