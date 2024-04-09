package com.jcca.admin.biz.vo;

import lombok.Data;

@Data
public class AssetTargetVo {

    private String targetAssetName;
    private String targetAssetId;
    private String localAssetId;
    private String localPortIndex;
    private String targetPortIndex;


}
