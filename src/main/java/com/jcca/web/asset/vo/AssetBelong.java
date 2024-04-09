package com.jcca.web.asset.vo;

import lombok.Data;

/**
 * @ClassName AssetBelong
 * @Description 资产归属
 * @Date 2020/6/23 10:51
 * @Author hanwone
 */
@Data
public class AssetBelong {

    /**
     * 机柜ID
     */
    private String cabinetId;
    /**
     * 机房ID
     */
    private String roomId;
    /**
     * 组织ID
     */
    private String orgId;
    /**
     * 机柜名称
     */
    private String cabinetName;
    /**
     * 机房名称
     */
    private String roomName;
    /**
     * 组织名称
     */
    private String orgName;
    /**
     * 开始U位
     */
    private Integer startPosition;
    /**
     * 结束U位
     */
    private Integer endPosition;

}
