package com.jcca.web.statistics.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName AssetStatisticsReq
 * @Description 设备统计分析请求参数
 * @Author wone
 * @Date 2021/1/21 14:25
 * @Version ITSM2.0
 **/
@Data
public class StatisticsAssetVo {

    /**
     * 设备类型统计
     */
    private List<StatisticsAlarmVo> assetModeList;

    /**
     * 设备型号统计
     */
    private List<StatisticsAlarmVo> assetImageList;

    /**
     * 设备厂商统计
     */
    private List<StatisticsAlarmVo> assetManufacturerList;

    /**
     * 设备信息
     */
    private List<AssetInfo> assetInfoList;

    /**
     * 总条数
     */
    private Long totalItem;

}
