package com.jcca.web2.vo;

import lombok.Data;

/**
 * @description: 车站网络参数
 * @author: Lvyp
 * @create: 2024/03/01 17:21
 */
@Data
public class NetObj {

    /**
     * 网络ID
     */
    private String netId;

    /**
     * 网络名称
     */
    private String netName;

    /**
     * 网关
     */
    private String gateway;

    /**
     * 通的数量
     */
    private Integer used;

    /**
     * 不通的IP数量
     */
    private Integer unused;

    private String  mask;

    private String remark;

}
