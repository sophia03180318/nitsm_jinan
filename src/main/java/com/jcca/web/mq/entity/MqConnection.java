package com.jcca.web.mq.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("MQ_CONNECTION")
public class MqConnection extends Model<MqConnection> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 连接名称
     */
    @TableField("connect_name")
    private String connectName;

    /**
     * 管道名称
     */
    @TableField("channel_name")
    private String channelName;

    /**
     * 主机IP
     */
    @TableField("connect_host")
    private String connectHost;

    /**
     * 主机端口
     */
    @TableField("connect_port")
    private Integer connectPort;

    /**
     * 用户ID
     */
    @TableField("USER_Id")
    private String userId;

    /**
     * 描述信息
     */
    @TableField("des_cription")
    private String description;

    /**
     * 总连接数
     */
    @TableField("CONNECTION_COUNT")
    private Integer connectionCount;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;


}