package com.jcca.web.graph.vo;

import lombok.Data;

/**
 * @ClassName TopoPortStatus
 * @Description 网络拓扑端口状态
 * @Date 2020/9/15 11:53
 * @Author hanwone
 */
@Data
public class TopoPortStatus {

    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 节点ID
     */
    private String nodeId;
    /**
     * 端口状态，0不正常，1正常
     */
    private Integer status;
}
