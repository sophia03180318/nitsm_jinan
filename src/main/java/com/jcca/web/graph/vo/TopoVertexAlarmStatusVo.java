package com.jcca.web.graph.vo;

import lombok.Data;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 15:34
 */
@Data
public class TopoVertexAlarmStatusVo implements java.io.Serializable {

    private String nodeId;
    /**
     * 告警状态
     */
    private int alarmLevel;


}
