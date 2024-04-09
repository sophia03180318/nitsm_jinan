package com.jcca.web2.vo;

import lombok.Data;

import java.util.List;

/**
 * @description: 网段信息
 * @author: sophia
 * @create: 2023/11/14 17:40
 **/
@Data
public class NetWorkVo {
    /**
     * 主键
     */
    private String id;

    /**
     * 网络名称
     */
    private String name;

    /**
     * IP地址
     * */
    private List<IpVo> ipList;

}