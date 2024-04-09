package com.jcca.web.ibmMQ.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * IBMMQ 监控措置表
 *
 * @author zhaozheng
 * @date 2020-07-28 09:49:20
 **/
@Data
@TableName("IBMMQ_MONITOR_MEASUREMENTS")
public class IBMMeasurements extends Model<IBMMeasurements> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 监控ID
     */
    @TableField("MONITOR_ID")
    private String monitorId;
    /**
     * 元素
     */
    @TableField("ELEMENT")
    private String element;

}