package com.jcca.web.event.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 告警组新增
 *
 * @author lyp
 */
@Data
public class EventGroupAddReq {

    /**
     * 告警标题
     */
    @NotEmpty(message = "请输入告警标题")
    private String name;
    /**
     * 告警类型 AlarmTypeEnum
     */
    @NotNull(message = "请选择告警类型")
    private Integer alarmType;
    /**
     * 事件类型ID
     */
    @NotEmpty(message = "请选择事件类型")
    private String eventTypeIds;
    /**
     * 告警级别
     */
    @NotNull(message = "请选择事件级别")
    private Integer levle;
    /**
     * 告警模板
     */
    @NotEmpty(message = "请填写告警模板")
    private String msgTemp;
    /**
     * 告警是否可恢复标识 1可以-1不可以 EventRecoverFlagEnum
     */
    @NotNull(message = "请选择告警是否可恢复")
    private Integer recoverFlag;
    /**
     * 逻辑与或关系标识 EventGroupLogicEnum
     */
    @NotNull(message = "请选择事件之间的逻辑关系")
    private Integer logicalFlag;
    /**
     * 是否启用级别告警
     * 1启用-1禁用
     */
    private Integer useStage;
    /**
     * 阶段告警配置
     */
    private List<StageConfigBean> stageConfigList;
}
