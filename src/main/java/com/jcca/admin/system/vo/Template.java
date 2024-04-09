package com.jcca.admin.system.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ Author：sophia
 * @ Date：Created in 10:16 2021/6/25
 * @ Description:
 */
@Data
public class Template implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 模板ID
     */
    private String id;
    /**
     * 模板名称
     */
    private String templateName;
    /**
     * 备注
     */
    private String remark;
    /**
     * 模板类型
     */
    private String type;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * 具有属性
     */
    //private String attribute;
    /**
     * 资产名称
     */
    private String name;
    /**
     * 资产类型
     */
    private String assetMode;
    /**
     * 资产型号
     */
    private String assetImage;
    /**
     * 资产厂商
     */
    private String manufacturerId;
    /**
     * 组织机构
     */
    private String orgId;
    /**
     * 机房
     */
    private String roomId;
    /**
     * 机柜
     */
    private String cabinetId;
    /**
     * 起始位置
     */
    private String startPosition;
    /**
     * 结束位置
     */
    private String endPosition;
    /**
     * ip地址1
     */
    private String ip;
    /**
     * ip地址2
     */
    private String ip2;
    /**
     * 资产编号
     */
    private String assetCode;
    /**
     * 是否监控
     */
    private String watch;
    /**
     * 是否topo显示
     */
    private String showTopo;
    /**
     * 是否采集系统时间
     */
    private String ntpFlag;

    /**
     * 是否为核心网络设备(核心交换机 核心路由器)
     */
    private String CR;

    /**
     * 采集类型
     */
    private String collectionType;
    /**
     * 登录用户名/团体名
     */
    private String osUser;
    /**
     * 登录密码
     */
    private String osPassword;
    /**
     * 管理口ip
     */
    private String ipmiIp;
    /**
     * 管理口账号
     */
    private String ipmiUser;
    /**
     * 管理口密码
     */
    private String ipmiPwd;
    /**
     * 登录端口
     */
    private String loginPort;
    /**
     * AB机标识
     */
    private String aBFlag;
    /**
     * 操作系统版本
     */
    private String operationSystem;
    /**
     * 下架时间
     */
    private String downlineTime;
    /**
     * 上架时间
     */
    private String onlineTime;
    /**
     * CPU型号
     */
    private String cpuModel;
    /**
     * CPU个数
     */
    private String cpuNumber;
    /**
     * CPU主频
     */
    private String cpuFrequency;
    /**
     * CPU核数
     */
    private String cpuCoreNumber;
    /**
     * 供货商
     */
    private String assetSupplier;
    /**
     * 主机运行方式
     */
    private String runModel;
    /**
     * 质保期限
     */
    private String validityDate;
    /**
     * 电源模块个数
     */
    private String powerTotal;
    /**
     * 电源模块型号
     */
    private String powerModel;
    /**
     * 单硬盘容量
     */
    private String diskCapacity;
    /**
     * 硬盘个数
     */
    private String diskTotal;
    /**
     * 内存
     */
    private String memory;
    /**
     * 序列号
     */
    private String serialNumber;
    /**
     * 主机编号
     */
    private String hostNumber;
    /**
     * 连接显示器数量
     */
    private String displayerTotal;
    /**
     * 视频线接口类型
     */
    private String displayerPortModel;
}
