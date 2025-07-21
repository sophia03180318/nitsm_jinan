package com.jcca.web.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author: hhw
 * @description: CollectCpuLoad 主要是用来
 * @date: 2025-07-03  16:46
 * @since: 2.0.15.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("COLLECT_CPU_LOAD")
public class CollectCpuLoad extends Model<CollectCpuLoad> {
    private static final long serialVersionUID = -5169492024413139842L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    private String assetId;
    private String collectCode;
    private Double loadOne;
    private Double loadFive;
    private Double loadFifteen;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
