package com.jcca.web2.dto.xunjian;

import lombok.Data;

import java.util.List;

/**
 * @author: hhw
 * @description: CollectExecResp 主要是用来接收实时巡检采集数据
 * @date: 2025-05-27  11:32
 * @since: 2.1.6.0
 */
@Data
public class CollectExecResp {

    private String assetId;

    private List<CollectExecResult> execRespList;
}
