package com.jcca.web.asset.controller.bean;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @ClassName AssetQueryReq
 * @Description 资产查询条件
 * @Date 2020/7/15 10:43
 * @Author hanwone
 */
@Data
public class AssetQueryReq {

    /**
     * 组织ID
     */
    private String orgId;

    private Byte watch;
    /**
     * 设备名称
     */
    private String name;
    /**
     * 设备IP
     */
    private String ip;
    /**
     * 新设备类型
     */
    private Integer desk;
    private List<Integer> desks;
    /**
     * 设备厂商
     */
    private Integer manufacturerId;
    /**
     * 是否大修设备
     */
    private Integer overhaul;

    private List<String> orgIds;
    /**
     * 自定义导出
     * key:字段
     * value:中文
     */
    private Map<String, String> fields;

    /**
     * 1监控管理, 2 资产管理
     */
    private Integer listEntrance;
    /**
     * 资产状态
     */
    private Integer status;

    /**
     * 监控状态
     */
    private String monitorStatus;

    /**
     * 排序字段
     */
    private String sort;

    /**
     * 排序方式
     */
    private String order;

    /**
     * 采集类型
     */
    private Integer collectionType;

    /**
     * 资产型号
     */
    private List<String> assetImages;

    /**
     * 运行方式
     */
    private String runModel;
    /**
     * 是否超期  1=超期了  2=没超期
     */
    private Integer overdue;

}
