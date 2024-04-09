package com.jcca.web.collect.service.bean;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class DicoveryRelatedInfo {
    //此设备的IP地址
    private String assetIp;
    //存放对端设备和
    private Map<String, List<TargetInfoVo>> map = new HashMap();
}
