package com.jcca.web.ibmMQ.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("IBMMQ_LISTENER_DATA")
public class IBMListenerData extends Model<IBMListenerData> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 捕获时间
     */
    @TableField("CAPTURETIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date captureTime;

    @TableField("LISTENERSTATUS")
    private Integer listenerStatus;

    /**
     * 监控ID
     */
    @TableField("MONITOR_ID")
    private String monitorId;

    @TableField("HEALTHSTATE")
    private String healthState;


}