package com.jcca.web.mq.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = true)
@TableName("MQ_MONITOR")
public class MqMonitor extends Model<MqMonitor> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     *种类  SEND_CHANNEL(发送通道)   RECEIVER_CHANNEL(接收通道)   MQQT_REMOTE(远程队列)  MQQT_LOCAL(本地队列) MQQT_TRANSMISSION(传输队列)
     */
    @TableField("category")
    private String category;

    /**
     * 名字
     */
    @TableField("name")
    private String name;

    /**
     * 阈值
     */
    @TableField("ruleValue")
    private Integer ruleValue;

    /**
     * 深度
     */
    @TableField("DEPTHS")
    private Integer depths;

    /**
     * 发
     */
    @TableField("SENT_KB")
    private long sentKb;

    /**
     * 收
     */
    @TableField("RCVD_KB")
    private long rcvdKb;

    /**
     * 状态
     */
    @TableField("STATE")
    private String state;

    /**
     * 业务组ID
     */
    @TableField("GROUP_ID")
    private String groupId;

    /**
     * 管理器ID
     */
    @TableField("CONNECT_ID")
    private String connectId;

    /**
     * 描述信息
     */
    @TableField("description")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

}

