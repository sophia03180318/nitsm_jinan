package com.jcca.web2.vo;


import lombok.Data;

/**
 * @author lifp
 * @version 1.0
 * @description: 通用组件 资产数据详情
 * @date 2025-10-09 星期四 11:25:28`
 */
@Data
public class CommonAssetInfo {
    // 资产(设备)ID
    private String assetId;
    // 资产(设备)名称
    private String assetName;
    // 资产类型
    private Integer assetMode;
    // 类型标识
    private String flag;
    // 组织ID
    private String orgId;
    // 配合前端模糊搜索
    private String ip2;
    private String ip;
}
