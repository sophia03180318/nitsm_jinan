package com.jcca.web.graph.vo;

import lombok.Data;

/**
 * @ClassName GraphStatusVo
 * @Description 机柜状态返回数据
 * @Date 2020/6/15 15:26
 * @Author hanwone
 */
@Data
public class GraphStatusVo {
    /**
     * 机柜ID
     */
    private String id;
    /**
     * 机柜状态取机柜内设备告警最严重级别返回
     */
    private int alarmLevel;
}
