package com.jcca.web2.vo;

import lombok.Data;

import java.util.List;

/**
 * @author HanHW
 * @description 设备巡检指标
 * @className InspectTargetVo
 * @date 2023/11/15 11:15
 * @since 2.1.0.0
 */
@Data
public class InspectTargetVo {

    // 指标总数
    private Integer targetTotal;
    // 正常指标数量
    private Integer normalCount;
    // 异常指标数量
    private Integer abnormalCount;
    // 巡检结果列表
    private List<InspectResultVo> targettList;

}
