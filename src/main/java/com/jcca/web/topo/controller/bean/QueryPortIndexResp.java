package com.jcca.web.topo.controller.bean;

import lombok.Data;

/**
 * 查询端口索引相应
 *
 * @author lyp
 */
@Data
public class QueryPortIndexResp {

    /**
     * 起始端口
     */
    private String startPort;
    /**
     * 结束端口
     */
    private String endPort;

}
