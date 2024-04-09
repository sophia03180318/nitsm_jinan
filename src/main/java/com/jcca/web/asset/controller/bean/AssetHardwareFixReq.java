package com.jcca.web.asset.controller.bean;

import lombok.Data;

import java.util.List;

/**
 * @ClassName AssetHardwareFixReq
 * @Description 资产硬件更换记录请求参数
 * @Date 2020/7/16 19:43
 * @Author hanwone
 */
@Data
public class AssetHardwareFixReq {

    private Integer page;

    private Integer size;

    private Integer start;

    private Integer end;

    /**
     * 设备名称
     */
    private String assetName;
    /**
     * 设备类型
     */
    private Integer assetMode;
    /**
     * 硬件类型
     */
    private Integer hardwareType;
    /**
     * 资产ID列表
     */
    private List<String> assetIdList;
    /**
     * 组织ID
     */
    private String orgId;

    private List<String> orgIdList;
    /*
     * 当前年月日
     */
    private String month;

    /*
     * 开始时间
     */
    private String startTime;
    /*
     * 结束时间
     */
    private String endTime;
}
