package com.jcca.web.alarm.entity;

import com.jcca.web.asset.entity.Asset;
import lombok.Data;

/**
 * redis告警推送记录
 **/
@Data
public class PushAlarm {
    private Asset asset;
    Byte blank;
    Byte alarmLevel;
}