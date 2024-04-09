package com.jcca.web2.vo;

import lombok.Data;

/**
 * @author HanHW
 * @description 大屏未确认告警推送Vo
 * @className RollAlarmVoV2
 * @date 2023/11/7 11:41
 * @since 2.1.0.0
 */
@Data
public class RollAlarmVo {

    // Web2Const.RIGHT_DOWN_ALARM
    private String code;

    private String username;

    private String orgName;

    private String assetName;

    private String assetId;

    private String orgId;

    private Integer alarmCount;

    private Integer alarmLevel;

    // 地图上车站x坐标
    private String stationx;
    // 地图上车站y坐标
    private String stationy;
}
