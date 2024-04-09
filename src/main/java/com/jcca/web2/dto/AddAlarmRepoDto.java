package com.jcca.web2.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @description: 新增告警规则
 * @author: Lvyp
 * @create: 2023/11/29 15:49
 */
@Data
public class AddAlarmRepoDto {

    /**
     * 名称
     */
    @NotEmpty(message = "缺少规则名称")
    private String name;
    /**
     * 告警级别
     * AlarmLevel
     */
    private Integer alarmLevel;
    /**
     * 知识库匹配码
     */
    @NotEmpty(message = "未传入匹配码")
    private String alarmCode;
    /**
     * 状态匹配码
     * 包含匹配
     */
    @NotEmpty(message = "未传入状态匹配字符")
    private String statusFlag;
    /**
     * 状态匹配码的类型
     * EventLevelEnum
     */
    @NotNull(message = "未传入状态匹配类型")
    private Integer flagType;
    /**
     * 事件类型ID
     */
    @NotEmpty(message = "未传入事件类型ID")
    private String eventTypeId;
    /**
     * 描述
     */
    private String descStr;
    /**
     * 解决方案
     */
    private String planStr;


    /**
     * 模板字符串
     */
    private String templateStr;

}
