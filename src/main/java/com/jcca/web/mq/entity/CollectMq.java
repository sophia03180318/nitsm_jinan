package com.jcca.web.mq.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_MQ")
public class CollectMq extends Model<CollectMq> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 种类  SEND_CHANNEL   RECEIVER_CHANNEL   MQQT_REMOTE  MQQT_LOCAL MQQT_TRANSMISSION
     */
    @TableField("category")
    private String category;
    /**
     * 名字
     */
    @TableField("name")
    private String name;

    /**
     * 深度
     */
    @TableField("DEPTHS")
    private String depths;

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
     * 连接ID
     */
    @TableField("CONNECTION_ID")
    private String connectionId;

    /**
     * 描述信息
     */
    @TableField("description")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

}