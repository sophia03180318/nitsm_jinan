package com.jcca.web2.dto;

import lombok.Data;

/**
 * @author: hhw
 * @description: AlarmCountDto主要是用来做告警统计
 * @date: 2025-04-10  09:52
 * @since: 2.1.5.0
 */
@Data
public class AlarmCountDto {

    private Integer total;
    private Integer level1;
    private Integer level2;
    private Integer level3;
    private Integer unHandled;
    private Integer handled;
}
