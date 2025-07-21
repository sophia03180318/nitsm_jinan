package com.jcca.web.asset.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Set;

/**
 * @ClassName AssetOutVo
 * @Description 外部获取资产参数
 * @Date 2020/7/31 10:35
 * @Author hanwone
 */
@Data
public class AssetOutVo {

    /**
     * 资产id
     */
    private String id;
    /**
     * 资产别名
     */
    private String name;
    /**
     * Ip地址
     */
    private String ip;

    /**
     * IP地址2
     */
    private String ip2;
    /**
     * 端口号
     */
    private Integer port;
    /**
     * 设备登录用户名
     */
    private String osUser;
    /**
     * 设备登录密码
     */
    private String osPassword;
    /**
     * 设备类型(183：主机，42：路由器，201：交换机，263：oracle数据库)
     */
    private Integer assetMode;
    /**
     * 生产厂商ID
     */
    private Integer manufacturerId;
    /**
     * 采集操作系统类型
     */
    private Integer collectionType;
    /**
     * 设备型号图片
     */
    private String assetImage;
    /**
     * 车站IP
     */
    private String stationIp;
    /**
     * 车站端口
     */
    private Integer stationPort;

    /**
     * 车站采集器代理地址
     */
    private String proxyUrl;

    /**
     * 组织ID
     */
    private String orgId;
    /**
     * ntp开启标识
     * 0：不采集，1：采集
     */
    private Byte ntpFlag;

    /**
     * 设备编号(分组编号)
     */
    private String assetCode;
    /**
     * 管理口IP
     */
    private String ipmiIp;
    /**
     * 管理口用户
     */
    private String ipmiUser;
    /**
     * 管理口密码
     */
    private String ipmiPwd;
    /**
     * 登录用户名
     */
    private String loginName;
    /**
     * 登录密码
     */
    private String loginPwd;

    private String loginPort;
    /**
     * 是否监控，1监控，0不监控
     */
    private Integer watch;
    /**
     * ab机标识
     */
    @JsonProperty("aBFlag")
    private Byte aBFlag;

    private Byte showTopo;
    /**
     * 应用服务器软件端口
     */
    @TableField(exist = false)
    private Set<Integer> ssPortSet;
    private Integer serviceType;

}
