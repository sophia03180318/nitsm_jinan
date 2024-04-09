package com.jcca.dataProcessing.Entity;


import lombok.Data;


@Data
public class CollectClusterEntity extends CommonEntity {


    private String id;

    /**
     * 设备IP
     */
    private String serverIp;

    private String serverIp2;
    /**
     * 设备名称
     */
    private String serverName;
    /**
     * 服务器状态
     */
    private String status;
    /**
     * 服务器角色
     */
    private String serverRole;
    /**
     * 采集时间
     */
    private Long collectTime;

    private String collectTimeStr;

}
