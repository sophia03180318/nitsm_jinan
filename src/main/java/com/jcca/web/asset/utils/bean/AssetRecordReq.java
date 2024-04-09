package com.jcca.web.asset.utils.bean;

import com.jcca.web.asset.utils.enums.AssetPlaceEnum;
import com.jcca.web.asset.utils.enums.ManufacturersEnum;
import lombok.Data;

import java.util.List;

@Data
public class AssetRecordReq {

    /**
     * 资产方位
     */
    private AssetPlaceEnum place;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产IP
     */
    private String assetIp;
    /**
     * 主机编号
     */
    private String hostNumber;
    /**
     * 调度台
     * 连接显示器数量
     */
    private String displayerTotal;
    /**
     * 调度台
     * 视频接口类型
     */
    private String displayerPortModel;
    /**
     * 上道时间
     */
    private String startDateStr;
    /**
     * 下道时间
     */
    private String endDateStr;
    /**
     * 安装路径
     */
    private String installPath;
    /**
     * 序列号
     */
    private String imei;
    /**
     * 质保期限
     */
    private String validityDateStr;
    /**
     * 设备厂家
     */
    private ManufacturersEnum manufacturers;
    /**
     * 运行模式
     * 双机热备/单机
     */
    private String runModel;
    /**
     * 操作系统
     */
    private String serverName;
    /**
     * 设备型号
     */
    private String assetImage;
    /**
     * 设备类型
     */
    private Integer assetMode;
    /**
     * 内存大小
     */
    private String memory;
    /**
     * 磁盘数量
     */
    private String diskTotal;
    /**
     * 单磁盘容量
     */
    private String diskCapacity;
    /**
     * 电源模块数量
     */
    private String powerTotal;
    /**
     * 电源型号
     */
    private String powerModel;
    /**
     * 服务器上应用信息
     */
    private List<ApplicationBase> applicationList;
    /**
     * 硬件动态
     */
    private List<HardwareBase> hardwareList;

}
