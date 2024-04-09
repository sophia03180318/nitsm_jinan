package com.jcca.web.collect.service.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class TopoRouteReturnVo {
    private String id;
    /**
     * 资产ID
     */
    private String assetId;

    //端口状态
    private Integer status;
    /**
     * 索引号
     * 匹配端口采集表中的 portIndexRank
     */
    private String portIndexRank;
    /**
     * 端口发送流量KB/S
     **/
    private String portSendNum;
    /**
     * 端口接收流量KB/S
     **/
    private String portReceiveNum;
    /**
     * 端口原始名称-vlan名称
     */
    private String portOrgName;
    /**
     * 端口名称
     */
    private String portName;
    /**
     * 本端端口IP
     */
    private String portIp;
    /**
     * 本段端口MAC地址
     */
    private String portMacAddress;
    /**
     * 端口索引名称
     */
    private String portIndexName;

    /**
     * 备注
     */
    private String remark;

    private List<TopoRouteTarget> targetList;
    /**
     * 采集时间
     **/
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createDate;


}
