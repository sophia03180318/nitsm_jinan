package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 采集端口信息
 *
 * @author Lvyp
 */
@Data
public class CollectInterfaceEntity extends  CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 端口名称
     */
    private String portName;
    /**
     * 端口别名
     */
    private String portAlias;
    /**
     * 端口类型
     */
    private int portType;
    /**
     * 端口索引
     */
    private String portIndex;
    /**
     * 端口排序
     */
    private int portIndexRank;
    /**
     * 端口连接类型1:half,2:full,3:disagree,4:auto
     * 1代表实体口其他都是非实体口
     */
    private int portLinkType;
    /**
     * 端口对应的IP地址
     */
    private String linkIp;
    /**
     * 端口对应网络的mask
     */
    private String linkMask;
    /**
     * 物理地址
     */
    private String linkPhyAddress;
    /**
     * 端口流入量
     */
    private long portIn;
    /**
     * 端口流出量
     */
    private long portOut;
    /**
     * 端口流入丢包数
     */
    private long discardPacketsIn;
    /**
     * 端口流出丢包数
     */
    private long discardPacketsOut;
    /**
     * 非单播流入量
     */
    private long noUnicastPacketsIn;
    /**
     * 非单播流出量
     */
    private long noUnicastPacketsOut;
    /**
     * 单播流入量
     */
    private long unicastPacketsIn;
    /**
     * 单播流出量
     */
    private long unicastPacketsOut;
    /**
     * 误码流入量
     */
    private long errorCodeIn;
    /**
     * 误码流出量
     */
    private long errorCodeOut;
    /**
     * 端口状态1：启用，2:空闲
     */
    private byte status;
    /**
     * 采集状态
     * 1通/0断
     * StatusEnum
     */
    private byte collectStatus;
    /**
     * 端口最大速率
     */
    private long portSpeed;
    /**
     * 端口接收速率
     */
    private long portInSpeed;
    /**
     * 端口发送速率
     */
    private long portOutSpeed;
    /**
     * 端口发送丢包率
     */
    private double losePacketsOutRate;
    /**
     * 端口接收丢包率
     */
    private double losePacketsInRate;
    /**
     * 端口发送误码率
     */
    private double erroCodeOutRate;
    /**
     * 端口接收误码率
     */
    private double erroCodeInRate;
    /**
     * 端口流出量
     */
    private double portOutRate;
    /**
     * 端口流出量
     */
    private double portInRate;

    /**
     * 端口CRC校验错误数
     */
    private long crcErrors;
    /**
     * 传输功率 dBm
     */
    private Double txPower;
    /**
     * 接收功率 dBm
     */
    private Double rxPower;
    /**
     * 采集包版本号
     * 新版本车站采集器会携带此参数
     */
    private String version;

}
