package com.jcca.component.other.bean;

import com.jcca.web.asset.entity.Asset;
import lombok.Data;

/**
 * ping 设备状态
 */
@Data
public class PingAssetStatus {
    //当前的ping状态
    private Boolean currStatus;
    private Asset asset;
}
