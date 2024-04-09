package com.jcca.web.ibmMQ.entity;

import cn.hutool.extra.ssh.ChannelType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("IBMMQ_CHANNEL_DATA")
public class IBMChannelData extends Model<IBMChannelData> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 管道名称
     */
    @Transient
    private String channelName;
    /**
     * 管道类型
     */
    @Transient
    private ChannelType channelType;
    /**
     * 管道状态
     */
    @TableField("CHANNELSTATUS")
    private Integer channelStatus;

    /**
     * 发送字节数
     */
    @TableField("BYTESSENT")
    private Integer bytesSent;
    /**
     * 接收字节数
     */
    @TableField("BYTESRECEIVED")
    private Integer bytesReceived;

    /**
     * 发送流量
     */
    @TableField("BUFFERSSENT")
    private Integer buffersSent;
    /**
     * 接收流量
     */
    @TableField("BUFFERSRECEIVED")
    private Integer buffersReceived;
    /**
     * 消息转换数
     */
    @TableField("MESSAGESTRANSFERRED")
    private Integer messagesTransferred;

    /**
     * 监控ID
     */
    @TableField("MONITOR_ID")
    private String monitorId;

    @TableField("HEALTHSTATE")
    private String healthState;

    /**
     * 捕获时间
     */
    @TableField("CAPTURETIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date captureTime;


}

