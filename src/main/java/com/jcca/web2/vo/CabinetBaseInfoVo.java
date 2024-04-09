package com.jcca.web2.vo;

import lombok.Data;

import java.util.List;

/**
 * @description: 查询机柜基础信息返回值
 * @author: Lvyp
 * @create: 2023/10/24 10:31
 */
@Data
public class CabinetBaseInfoVo {
    /**
     * 机柜ID
     */
    private String cabinetId;
    /**
     * 机柜名称
     */
    private String cabinetName;
    /**
     * 总U位
     */
    private Integer seatCount;
    /**
     * 已使用U位
     */
    private Integer usedSeat;
    /**
     * 机柜内资产数量
     */
    private Integer assetSize;
    /**
     * 机柜位置
     */
    private String position;
    /**
     * 机柜内的资产列表
     */
    private List<CabinetBaseAssetVo> assetList;

}
