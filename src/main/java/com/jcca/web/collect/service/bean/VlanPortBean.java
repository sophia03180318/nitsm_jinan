package com.jcca.web.collect.service.bean;

import lombok.Data;

/**
 * vlan-端口关系
 *
 * @author lyp
 */
@Data
public class VlanPortBean {

    /**
     * vlanID
     */
    private String vlanId;
    /**
     * 物理端口的索引
     */
    private String portIndex;
    /**
     * vlan 下物理端口的对端MAC地址
     */
    private String macAddress;
    /**
     * 序号
     */
    private String rank;


}
