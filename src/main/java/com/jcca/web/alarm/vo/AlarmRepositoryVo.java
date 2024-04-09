package com.jcca.web.alarm.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 告警知识库记录
 *
 * @author Lvyp
 */
@Data
public class AlarmRepositoryVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * 告警级别
     */
    private Integer alarmLevel;
    /**
     * 告警级别
     * AlarmLevel
     */
    private String alarmLevelStr;
    /**
     * 告警匹配码
     */
    private String alarmCode;
    /**
     * 设备类型
     */
    private String assetModeStr;
    /**
     * 状态匹配码
     * 包含匹配
     */
    private String statusFlag;
    /**
     * 状态匹配码的类型
     */
    private Integer flagType;
    /**
     * 状态类型翻译后的信息
     */
    private String flagTypeStr;

    private String eventTypeId;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 总结描述
     */
    private String descStr;
    /**
     * 建议方案
     */
    private String planStr;

    /**
     * 事件类型
     */
    private String eventTypeStr;

    /**
     * 模板字符串
     */
    private String templateStr;

}
