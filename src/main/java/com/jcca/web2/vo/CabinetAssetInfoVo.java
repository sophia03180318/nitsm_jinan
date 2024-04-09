package com.jcca.web2.vo;

import lombok.Data;

/**
 * @description: 机柜内设备信息
 * @author: Lvyp
 * @create: 2023/10/24 15:09
 */
@Data
public class CabinetAssetInfoVo {

    /**
     * 设备名称
     */
    private String name;
    /**
     * 设备ID
     */
    private String id;
    /**
     * 资产型号
     */
    private String assetImage;
    /**
     * 设备类型(183：主机，42：路由器，201：交换机，263：oracle数据库,318存储)
     */
    private Integer assetMode;
    /**
     * 设备所在机柜名字
     */
    private String cabinetName;
    /**
     * 机柜中设备位置
     * xx机柜xxU
     */
    private String cabinetPosition;
    /**
     * 设备位置
     * xx机房xx机柜xxU
     */
    private String position;
    /**
     * 设备IP1
     */
    private String ip1;
    /**
     * 设备IP2
     */
    private String ip2;

    public CabinetAssetInfoVo() {

    }

    public CabinetAssetInfoVo(CabinetAssetInfoVo vo) {
        this.name = vo.getName();
        this.assetImage = vo.getAssetImage();
        this.assetMode = vo.getAssetMode();
        this.cabinetName = vo.getCabinetName();
        this.cabinetPosition = vo.getCabinetPosition();
        this.position = vo.getPosition();
        this.ip1 = vo.getIp1();
        this.ip2 = vo.getIp2();
    }

}
