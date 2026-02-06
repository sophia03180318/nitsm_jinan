package com.jcca.web.graph.vo;

import lombok.Data;

import java.util.List;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/8/24 14:36
 */
@Data
public class TopoPortInfoVo {
    /**
     * 端口名称
     */
    private String portName;
    /**
     * 端口状态
     */
    private Integer status;
    /**
     * 端口排序
     */
    private Integer portIndexRank;
    /**
     * 物理地址
     */
    private String phyAddress;
    /**
     * 端口速率(端口带宽 非实时速率)
     */
    private Integer portSpeed;
    /**
     * 传输功率 dBm
     */
    private String txPower;
    /**
     * 接收功率 dBm
     */
    private String rxPower;
    /**
     * 流入丢包总数
     */
    private String inDiscardPackage;
    /**
     * 流出丢包总数
     */
    private String outDiscardPackage;
    /**
     * 流入误包总数
     */
    private String inErrorCode;
    /**
     * 流出误包总数
     */
    private String outErrorCode;
    /**
     * 端口流入速率
     */
    private List<StatisticsInfoVo> portIns;
    /**
     * 端口流出速率
     */
    private List<StatisticsInfoVo> portOuts;
    /**
     * 端口流入丢包率
     */
    private List<StatisticsInfoVo> discardPackageIns;
    /**
     * 端口流出丢包率
     */
    private List<StatisticsInfoVo> discardPackageOuts;
    /**
     * 端口流入误码率
     */
    private List<StatisticsInfoVo> errorCodeIns;
    /**
     * 端口流出误码率
     */
    private List<StatisticsInfoVo> errorCodeOuts;
    /**
     * 光模块状态
     */
    private String moduleState;

    /**
     * CRC校验错误数
     */
    private Integer crcErrors;

    /**
     * 端口传入光功率
     */
    private List<StatisticsInfoVo> dbmIn;

    /**
     * 端口传出光功率
     */
    private List<StatisticsInfoVo> dbmOut;

    /**
     * 端口别名
     */
    private String portAlias;
}
