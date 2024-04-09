package com.jcca.web.ibmMQ.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

@Data
@TableName("IBMMQ_QUEUE_DATA")
public class IBMQueueData extends Model<IBMQueueData> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
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
     * 当前深度
     */
    @TableField("CURRENTQDEPTH")
    private Integer currentQDepth;
    /**
     * 最大深度
     */
    @TableField("MAXQDEPTH")
    private Integer maxQDepth;
    /**
     * 打开输入个数
     */
    @TableField("OPENINPUTCOUNT")
    private Integer openInputCount;
    /**
     * 打开输出个数
     */
    @TableField("OPENOUTPUTCOUNT")
    private Integer openOutputCount;
    /**
     * 监控ID
     */
    @TableField("MONITOR_ID")
    private String monitorId;


}