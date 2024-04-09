package com.jcca.web.alarm.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 新增告警知识库
 *
 * @author Lvyp
 */
@Data
public class AddAlarmRepoReq implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 名称
     */
    @NotEmpty(message = "请输入告警名称")
    private String name;
    /**
     * 告警级别
     * AlarmLevel
     */
    @NotNull(message = "请输入告警级别")
    private Integer alarmLevel;
    /**
     * 事件匹配码
     */
    @NotEmpty(message = "请输入告警匹配编号")
    private String alarmCode;
    /**
     * 状态匹配码
     * 包含匹配
     */
    @NotEmpty(message = "请输入状态匹配关键字")
    private String statusFlag;
    /**
     * 状态匹配码的类型
     */
    @NotNull(message = "请输入状态类型")
    private Integer flagType;
    /**
     * 设备类型
     */
    private Integer assetMode;
    /**
     * 总结描述
     */
    private String descStr;
    /**
     * 建议方案
     */
    private String planStr;

    /**
     * 事件类型ID
     */
    private String eventTypeId;

    /**
     * 告警知识库ID
     */
    private String alarmRepoId;
    /**
     * 模板字符串
     */
    private String templateStr;

}
