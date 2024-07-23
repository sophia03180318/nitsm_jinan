package com.jcca.web.common.controller.bean;

import lombok.Data;

import java.util.List;

/**
 * @description: ITSM告警规则
 * @author: Lvyp
 * @create: 2024/04/19 18:19
 */
@Data
public class ItsmAlarmRuleResp {

    private String assetId;

    /**
     * 进程配置
     */
    private List<ItsmProcessConf> processList;

    /**
     * 阈值配置
     */
    private List<ItsmThresholdConf> thresholdConfList;


}
