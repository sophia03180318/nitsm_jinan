package com.jcca.web.ibmMQ.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.jcca.web.ibmMQ.annotation.PCFParam;
import lombok.Data;

import java.util.Date;

@Data
@TableName("IBMMQ_TOPIC_DATA")
public class IBMTopicData extends Model<IBMTopicData> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 获取时间
     */
    @TableField("CAPTURETIME")
    private Date captureTime;
    /**
     * 健康状态
     */
    @TableField("HEALTHSTATE")
    private String healthState;
    /**
     * 发布消息个数
     */
    @TableField("PUBLISHEDMESSAGESCOUNT")
    @PCFParam(option = 12)
    private Integer publishedMessagesCount;
    /**
     * 发布的个数
     */
    @TableField("PUBLISHERSCOUNT")
    @PCFParam(value = 215, option = 11)
    private Integer publishersCount;
    /**
     * 子消息个数
     */
    @TableField("SUBSCRIBERSCOUNT")
    @PCFParam(value = 204, option = 11)
    private Integer subscriberscount;
    /**
     * 监控ID
     */
    @TableField("MONITOR_ID")
    private String monitorId;
}