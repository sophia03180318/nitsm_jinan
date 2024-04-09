package com.jcca.web.event.controller.bean;

import com.jcca.web.event.entity.AlarmEventGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 查询响应
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventGroupQueryResp extends AlarmEventGroup {

    private static final long serialVersionUID = 1L;

    private String alarmTypeStr;

    private List<String> eventTypeIdList;

    private String eventTypeNameStr;

    private String recoverFlagStr;

    private String logicalFlagStr;

    /**
     * 阶段告警配置
     */
    private List<StageConfigBean> stageConfigList;

}
