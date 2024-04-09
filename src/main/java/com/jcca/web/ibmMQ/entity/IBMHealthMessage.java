package com.jcca.web.ibmMQ.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcca.web.ibmMQ.vo.HealthState;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("IBMMQ_HEALTH_MESSAGE")
public class IBMHealthMessage extends Model<IBMHealthMessage> implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    @TableField("LEVEL_VALUE")
    private HealthState level;
    @TableField("RULE")
    private String rule;
    @TableField("RESULT")
    private String result;
    @TableField("TIME")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    @TableField("STATISTIC_ID")
    private String statisticId;
}