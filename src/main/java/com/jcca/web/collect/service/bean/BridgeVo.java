package com.jcca.web.collect.service.bean;

import lombok.Data;

@Data
public class BridgeVo {
    //所属vlan
    private String belongVlan;
    private String targetMac;
}
