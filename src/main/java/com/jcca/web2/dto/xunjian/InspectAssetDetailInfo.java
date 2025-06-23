package com.jcca.web2.dto.xunjian;

import lombok.Data;

/**
 * @author: hhw
 * @description: InspectAssetDetailInfo 主要是用来
 * @date: 2025-05-22  14:58
 * @since: 2.1.6.0
 */
@Data
public class InspectAssetDetailInfo {

    private String inspectCode;

    private String assetId;
    private String assetName;
    private String assetIp1;
    private Integer assetDesk;
    private String inspectState;
    private String orgName;
    private String roomName;
    private String cabinetName;
}
