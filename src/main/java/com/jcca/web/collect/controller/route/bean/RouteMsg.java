package com.jcca.web.collect.controller.route.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcca.web.asset.entity.Asset;
import lombok.Data;

import java.util.Date;

/**
 * 对端信息
 *
 * @author lyp
 */
@Data
public class RouteMsg {

    /**
     * 数据ID
     **/
    private String id;
    /**
     * 本地资产
     **/
    private Asset localAsset;
    /**
     * 本地端口
     **/
    private String localPort;
    /**
     * 本地端口IP
     **/
    private String localIp;
    /**
     * 本地端口mac地址
     **/
    private String localMacAddress;
    /**
     * 端口状态
     **/
    private Byte portStatus;
    /**
     * 端口发送流量KB/S
     **/
    private String portSendNum;
    /**
     * 端口接收流量KB/S
     **/
    private String portReceiveNum;

    /**
     * 远程资产IP
     **/
    private String remoteIp;
    /**
     * 远程资产名称
     **/
    private String remoteName;

    private String remoteMacAddress;
    /**
     * 采集时间
     **/
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectDate;

    /**
     * 远程资产
     **/
    private Asset remoteAsset;
    /**
     * 远程端口
     **/
    private String remotePort;

    private String portIndexRank;

    private String remark;
    /**
     * 端口类型
     */
    private Integer type;

}
