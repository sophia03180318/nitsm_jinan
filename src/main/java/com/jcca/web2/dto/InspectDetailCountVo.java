package com.jcca.web2.dto;

import lombok.Data;

/**
 * @author: hhw
 * @description: InspectDetailCountVo 主要是用来
 * @date: 2025-05-22  14:58
 * @since: 2.1.6.0
 */
@Data
public class InspectDetailCountVo {

    private String inspectCode;
    private String operator;

    private String assetDesk;
    private String deskName;

    private Integer total;
    private Integer totalNormal;
    private Integer totalAbnormal;
}
