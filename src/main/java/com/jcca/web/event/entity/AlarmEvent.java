package com.jcca.web.event.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 告警事件
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ALARM_EVENT")
public class AlarmEvent extends Model<AlarmEvent> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 事件类型ID
     */
    @TableField("EVENT_TYPE_ID")
    private String eventTypeId;
    /**
     * 知识库ID
     */
    @TableField("REPOSITORY_ID")
    private String repositoryId;
    /**
     * 事件原始信息
     */
    @TableField("EVENT_MSG")
    private String eventMsg;
    /**
     * 事件翻译后的信息
     */
    @TableField("REPO_MSG")
    private String repoMsg;
    /**
     * 收到的事件标识
     */
    @TableField("UNIQUE_CODE")
    private String uniqueCode;
    /**
     * 基础值
     */
    @TableField("BASE_VALUE")
    private String baseValue;
    /**
     * 采集到的值
     */
    @TableField("COLLECT_VALUE")
    private String collectValue;
    /**
     * 一些告警的特殊设定值
     * 如：进程告警：放入进程名称
     * 端口告警 ：放入端口索引
     * 网卡告警：放入网卡名称
     */
    @TableField("FLAG")
    private String flag;

    /**
     * 事件的匹配类型
     * 如果两个事件匹配标记相同则告警的时候需要验证alarmCode
     */
    @TableField("MATCH_FLAG")
    private String matchFlag;
    /**
     * EventLevelEnum
     * 事件性质（异常事件、恢复事件、通知事件）
     */
    @TableField("EVENT_LEVEL")
    private Integer eventLevel;
    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "CREATE_TIME")
    private Date createTime;
    /**
     * 更新时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "UPDATE_TIME")
    private Date updateTime;

    @TableField(exist = false)
    private String createTimeStr;
}
