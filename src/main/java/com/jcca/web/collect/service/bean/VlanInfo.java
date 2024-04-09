package com.jcca.web.collect.service.bean;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class VlanInfo {

    private String assetIp;

    //(端口索引,vlan索引)
    private Map<String, String> map = new HashMap();
}
