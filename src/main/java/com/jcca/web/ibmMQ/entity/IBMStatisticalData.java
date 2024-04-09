/*     */
package com.jcca.web.ibmMQ.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

@Data
@TableName("IBMMQ_STATISTICS_DATA")
public class IBMStatisticalData extends Model<IBMStatisticalData> implements java.io.Serializable {

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
     * 监控ID
     */
    @TableField("MONITOR_ID")
    private String monitorId;
    /**
     *
     */
    @TableField("DTYPE")
    private String dType;
}