package com.jcca.web.statistics.vo;

import lombok.Data;

/**
 * @ClassName AssetInfo
 * @Description 设备统计中设备信息
 * @Author wone
 * @Date 2021/1/21 15:09
 * @Version ITSM2.0
 **/
@Data
public class AssetInfo {

    private String assetId;
    private String assetName;
    private String assetIp;
    private String assetModeStr;
    private String assetManufacturer;
    private String orgName;
    private String cabinetName;
    private String assetPosition;
}
