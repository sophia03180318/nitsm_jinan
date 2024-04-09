package com.jcca.admin.system.config.bean;

import lombok.Data;

/**
 * 系统功能模块配置请求
 * 1:中心采集器,2:车站采集器,3:itsm,4:卡斯柯接口服务
 */
@Data
public class SysModuleConfigReq {
    //车站采集器IP
    private String stationIp;
    //服务类型 1:中心采集器,2:车站采集器,3:itsm,4:卡斯柯接口服务
    private Integer serviceType;
    //组织结构id
    private String orgId;

}
