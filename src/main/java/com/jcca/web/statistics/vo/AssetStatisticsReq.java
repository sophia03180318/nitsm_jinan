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
public class AssetStatisticsReq {

    /**
     * 设备名称
     */
    private String assetName;
    /**
     * 组织ID
     */
    private String orgId;
    /**
     * 设备类型
     */
    private Integer desk;
    /**
     * 设备型号
     */
    private String assetImage;
    /**
     * 厂商ID
     */
    private Integer manufacturerId;
    /**
     * 设备IP
     */
    private String assetIp;
    /**
     * 分页起始行号
     */
    private Integer start;
    /**
     * 分页结束行号
     */
    private Integer end;

    private List<String> orgIdList;

    private Integer page;
    private Integer size;
    private String showJcca;
}
