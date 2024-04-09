package com.jcca.web.ibmMQ.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("IBMMQ_MONITOR")
public class IBMMonitor extends Model<IBMMonitor> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_MONITOR_NAME = "qmgr";

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 种类
     */
    @TableField("category")
    private String category;
    /**
     * 数据期望时间
     */
    @TableField("dataExpirationTime")
    private String dataExpirationTime;
    /**
     * 描述信息
     */
    @TableField("description")
    private String description;
    /**
     * 健康规则
     */
    @TableField("healthRule")
    private String healthRule;
    /**
     * 名字
     */
    @TableField("name")
    private String name;
    /**
     * 对象名字
     */
    @TableField("objectName")
    private String objectName;
    /**
     * 对象类型
     */
    @TableField("objectType")
    private String objectType;

    /**
     * 队列用法\是否是传输队列
     */
    @TableField("usage")
    private String usage;

    /**
     * 获取间隔
     */
    @TableField("pollingInterval")
    private String pollingInterval;
    /**
     * 范围
     */
    @TableField("scope")
    private String scope;
    /**
     * 滑动窗口
     */
//    @TableField("slidingWindow")
//    private String slidingWindow;
    /**
     * 状态
     */
    @TableField("STATE")
    private String state;
    /**
     * 展示类型
     */
    @TableField("viewType")
    private String viewType;
    /**
     * 连接ID
     */
    @TableField("CONNECTIONID")
    private String connectionId;

    /**
     * 业务组ID
     */
    @TableField("GROUP_ID")
    private String groupId;

    @TableField("MEASUREMENT")
    private String measurement;

    @TableField("HEALTHSTATUS")
    private String healthStatus;
    /**
     * 连接名字
     */
    @TableField(exist = false)
    private String connectionName;

    @TableField(exist = false)
    private String channelName;
    @TableField(exist = false)
    private String host;
    @TableField(exist = false)
    private Integer port;
    @TableField(exist = false)
    private String userId;

    @TableField(exist = false)
    private String connectionDescription;

    @TableField(exist = false)
    private String currentQDepth;

    @TableField(exist = false)
    private String maxQDepth;

    @TableField(exist = false)
    private long bytesSent;

    @TableField(exist = false)
    private long bytesReceived;

    @TableField(exist = false)
    private String sentStr;

    @TableField(exist = false)
    private String receivedStr;
}

