package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 告警白名单 在此名单内的设备将不再产生此类告警
 *
 * @description: 告警白名单
 * @author: Lvyp
 * @create: 2023/11/30 10:38
 */
@TableName("ALARM_WHITE_LIST")
@Data
public class AlarmWhitelist extends Model<AlarmWhitelist> {


    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 一些告警的特殊设定值
     * 如：进程告警：放入进程名称
     * 端口告警 ：放入端口索引
     * 网卡告警：放入网卡名称
     */
    @TableField("FLAG")
    private String flag;

    /**
     * 事件匹配码
     */
    @TableField("ALARM_CODE")
    private String alarmCode;

    /**
     * 事件类型ID
     */
    @TableField(value = "EVENT_TYPE_ID")
    private String eventTypeId;

    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * 创建者
     */
    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;

}
