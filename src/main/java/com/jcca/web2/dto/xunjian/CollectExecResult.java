package com.jcca.web2.dto.xunjian;

import com.jcca.component.dto.ReceiveCollectDto;
import lombok.Data;

/**
 * @author: hhw
 * @description: CollectExecResult 主要是用来接收实时巡检采集数据
 * @date: 2025-05-27  11:38
 * @since: 2.1.6.0
 */
@Data
public class CollectExecResult {
    private Integer code;
    private String msg;
    private String assetId;
    private String targetHandle;
    private String redisQueue;
    private ReceiveCollectDto result;

}
