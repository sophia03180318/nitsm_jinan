package com.jcca.web.ibmMQ.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

@Data
@TableName("IBMMQ_QMGR_DATA")
public class IBMQMgrData extends Model<IBMQMgrData> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 队列管理器状态
     */
    @TableField("QUEUEMANAGERSTATUS")
    private Integer queueManagerStatus;


    @TableField("HEALTHSTATE")
    private String healthState;

    /**
     * 监控ID
     */
    @TableField("MONITOR_ID")
    private String monitorId;

    /**
     * 获取时间
     */
    @TableField("CAPTURETIME")
    private Date captureTime;


}