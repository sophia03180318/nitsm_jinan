package com.jcca.web.nodes.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 中心采集器节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("CENTER_NODES")
public class CenterNodes extends Model<CenterNodes> {

    /**
     * 节点ID
     */
    @TableId(value = "NODE_ID", type = IdType.ID_WORKER_STR)
    private String nodeId;
    /**
     * 节点IP1
     */
    @TableField("NODE_IP1")
    private String  nodeIp1;
    /**
     * 节点IP2
     */
    @TableField("NODE_IP2")
    private String nodeIp2;

    @TableField("CREATE_TIME")
    private Date createTime;

    @TableField("UPDATE_TIME")
    private Date updateTime;
}
