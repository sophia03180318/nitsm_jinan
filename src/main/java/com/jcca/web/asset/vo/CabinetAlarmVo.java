package com.jcca.web.asset.vo;

import lombok.Data;

/**
 * @ClassName CabinetAlarmVo
 * @Description 带告警级别的机柜列表
 * @Date 2020/4/27 10:34
 * @Author hanwone
 */
@Data
public class CabinetAlarmVo {
    private String id;
    private String name;
    private int alarmLevel;
}
