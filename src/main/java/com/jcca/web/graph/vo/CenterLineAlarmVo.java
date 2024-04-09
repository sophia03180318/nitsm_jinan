package com.jcca.web.graph.vo;

import lombok.Data;

/**
 * @Description 西宁线路图展示
 * @ClassName CenterLineAlarmVo
 * @Date 2022/5/25 9:55
 * @Author hanwone
 * @Since 2.0.0.1
 */
@Data
public class CenterLineAlarmVo {

    private String orgId;
    private String orgName;
    private Integer alarmLevel;
    private Integer orgType;
}
