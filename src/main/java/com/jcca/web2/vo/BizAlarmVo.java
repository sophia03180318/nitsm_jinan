package com.jcca.web2.vo;

import lombok.Data;

/**
 * @author HanHW
 * @description 各厂家告警按类型统计
 * @className BizAlarmVo
 * @date 2023/10/30 11:32
 * @since 2.1.0.0
 */
@Data
public class BizAlarmVo {

    // 业务代码  BizTypeEnum
    private String bizType;

    // 厂家名称  BizTypeEnum
    private String bizName;

    // 厂家设备数量
    private Integer assetCount;

    // 正常设备数量
    private Integer normalCount;

    // 异常设备数量
    private Integer abnormalCount;

}
