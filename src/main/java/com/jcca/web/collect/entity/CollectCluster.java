package com.jcca.web.collect.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


/**
 * 集群信息
 * @author lyp
 */
@Data
@TableName("COLLECT_CLUSTER")
public class CollectCluster {

    @TableId("ID")
    private String id;
    /**
     * 对应资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 设备IP
     */
    @TableField("SERVER_IP")
    private String serverIp;

    @TableField("SERVER_IP2")
    private String serverIp2;
    /**
     * 设备名称
     */
    @TableField("SERVER_NAME")
    private String serverName;
    /**
     * 服务器状态
     */
    @TableField("STATUS")
    private String status;
    /**
     * 服务器角色
     */
    @TableField("SERVER_ROLE")
    private String serverRole;
    /**
     * 采集时间
     */
    @TableField("COLLECT_TIME")
    private Date collectTime;

    @TableField(exist = false)
    private String collectTimeStr;

}
