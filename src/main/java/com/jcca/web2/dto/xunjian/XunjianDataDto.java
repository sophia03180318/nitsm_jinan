package com.jcca.web2.dto.xunjian;

import lombok.Data;

/**
 * @author: hhw
 * @description: XunjianDataDto 主要是用来
 * @date: 2025-05-27  17:02
 * @since: 2.1.6.0
 */
@Data
public class XunjianDataDto {

    private String jobId;
    private String assetId;
    // 巡检指标项 StatusInfoChangeTypeEnum 类中code值
    private String targetItem;
    // 3正常，4异常  Web2Const
    private String inspectState;
    // 阈值设定值
    private String thresholdValue;
    // 巡检结果值
    private String inspectValue;
    // 巡检结果描述 相应告警信息
    private String resultMsg;
}
