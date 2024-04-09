package com.jcca.web2.vo;

import lombok.Data;

/**
 * @author HanHW
 * @description 中心业务拓扑
 * @className BizTopoCenterVo
 * @date 2024/1/24 13:28
 * @since 2.1.0.0
 */
@Data
public class BizTopoCenterVo {

    private String assetId;
    private String assetName;
    private String assetIp;
    private String orgId;
    // 业务类型ID
    private String serviceTypeId;
    // 有无告警  0无告警，1有告警
    private Integer alarmStatus;
    // 主备机状态  MASTER主机，BACK备机, STOP中断，DEFNULL未知
    // HostRunStatusEnum
    private String hostType;
}
