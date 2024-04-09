package com.jcca.web.asset.detail.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jcca.web.collect.controller.route.bean.RouteMsg;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName DetailNetWorkCard
 * @Description 资产详情----网卡信息
 * @Date 2020/6/29 17:35
 * @Author hanwone
 */
@Data
public class DetailNetWorkCard {
    /**
     * 网卡名字
     */
    private String name;
    /**
     * 网卡IP
     */
    private String ip;
    /**
     * 网卡状态
     * 1通/0断
     * CollectNetCardStatus
     */
    private Byte status;

    private RouteMsg routeMsg;
    /**
     * mac 地址
     */
    private String macAddress;
    /**
     * 采集时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectTime;
}
