package com.jcca.component.client.bean;

import lombok.Data;

/**
 * 采集节点信息
 *
 * @author lyp
 */
@Data
public class CollectNodesMsg {

    public static final String UP = "0";
    public static final String DOWN = "1";

    public static final String IS_CENTER = "0";
    public static final String IS_STATION = "1";

    private String nodeName;

    private String nodeUrl;
    /**
     * 0在线、1离线
     */
    private String nodeState;

    private String nodeVersion;

    private String nodeIp;
    /**
     * 0中心采集器1车站采集器
     */
    private String nodeType;

}
