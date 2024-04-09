package com.jcca.component.thresholds.bean;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * 采集端口信息
 *
 * @author Lvyp
 */
@Data
public class CollectInterfaceBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 采集时间
     */
    @NotEmpty(message = "采集时间不能空")
    private String collectTime;
    /**
     * 资产ID
     */
    @NotEmpty(message = "资产ID不能空")
    private String assetId;
    /**
     * 资产IP
     */
    @NotEmpty(message = "资产ip不能空")
    private String assetIp;
    /**
     * 端口名称
     */
    @NotEmpty(message = "端口名称空")
    private String portName;
    /**
     * 端口别名
     */
    @NotEmpty(message = "端口别名空")
    private String portAlias;
    /**
     * 端口类型
     */
    @NotNull(message = "端口类型空")
    private Integer portType;
    /**
     * 端口索引
     */
    @NotEmpty(message = "端口索引")
    private String portIndex;
    /**
     * 端口排序
     */
    @NotNull(message = "端口索引排序不能空")
    private Integer portIndexRank;
    /**
     * 端口连接类型1:half,2:full,3:disagree,4:auto
     */
    @NotNull(message = "端口链接类型空")
    private Integer portLinkType;
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
    @NotNull(message = "端口流入量空")
    private Long portIn;
    /**
     * 端口流出量
     */
    @NotNull(message = "端口流出量空")
    private Long portOut;
    /**
     * 端口流入丢包数
     */
    @NotNull(message = "端口流入丢包数空")
    private Long discardPacketsIn;
    /**
     * 端口流出丢包数
     */
    @NotNull(message = "端口流出丢包数空")
    private Long discardPacketsOut;
    /**
     * 非单播流入量
     */
    @NotNull(message = "非单播流入量空")
    private Long noUnicastPacketsIn;
    /**
     * 非单播流出量
     */
    @NotNull(message = "非单播流出量空")
    private Long noUnicastPacketsOut;
    /**
     * 单播流入量
     */
    @NotNull(message = "单播流入量空")
    private Long unicastPacketsIn;
    /**
     * 单播流出量
     */
    @NotNull(message = "单播流出量空")
    private Long unicastPacketsOut;
    /**
     * 误码流入量
     */
    @NotNull(message = "误码流入量空")
    private Long errorCodeIn;
    /**
     * 误码流出量
     */
    @NotNull(message = "误码流出量空")
    private Long errorCodeOut;
    /**
     * 端口状态1：启用，2:空闲
     */
    @NotNull(message = "端口状态空")
    private Byte status;
    /**
     * 采集状态
     * 1通/0断
     * StatusEnum
     */
    @NotNull(message = "采集状态空")
    private Byte collectStatus;
    /**
     * 端口最大速率
     */
    @NotNull(message = "端口速率空")
    private Long portSpeed;
    /**
     * 端口接收速率
     */
    @NotNull(message = "端口接收速率")
    private Long portInSpeed;
    /**
     * 端口发送速率
     */
    @NotNull(message = "端口发送速率")
    private Long portOutSpeed;
    /**
     * 端口发送丢包率
     */
    @NotEmpty(message = "端口发送丢包率空")
    private String losePacketsOutRate;
    /**
     * 端口接收丢包率
     */
    @NotEmpty(message = "端口接收丢包率空")
    private String losePacketsInRate;
    /**
     * 端口发送误码率
     */
    @NotEmpty(message = "端口发送错误率空")
    private String erroCodeOutRate;
    /**
     * 端口接收误码率
     */
    @NotEmpty(message = "端接收错误率空")
    private String erroCodeInRate;

    /**
     * 端口CRC校验错误数
     */
    private Long crcErrors;
    /**
     * 传输功率 dBm
     */
    private String txPower;
    /**
     * 接收功率 dBm
     */
    private String rxPower;


    /**
     * 验证必填数据是否都填写了
     * true 填写了  false 存在没有填写的值
     * @param item
     * @return
     */
    public static Boolean verifyData(CollectInterfaceBean item){
        return !(StrUtil.isEmpty(item.getCollectTime()) || StrUtil.isEmpty(item.getAssetId())
                || StrUtil.isEmpty(item.getAssetIp()) || StrUtil.isEmpty(item.getPortName())
                || Objects.isNull(item.getPortType()) || StrUtil.isEmpty(item.getPortIndex())
                || Objects.isNull(item.getPortIndexRank()) || Objects.isNull(item.getPortIn())
                || Objects.isNull(item.getPortOut()) || Objects.isNull(item.getDiscardPacketsIn())
                || Objects.isNull(item.getNoUnicastPacketsIn()) || Objects.isNull(item.getDiscardPacketsOut())
                || Objects.isNull(item.getNoUnicastPacketsOut()) || Objects.isNull(item.getUnicastPacketsIn())
                || Objects.isNull(item.getUnicastPacketsOut()) || Objects.isNull(item.getErrorCodeIn())
                || Objects.isNull(item.getErrorCodeOut()) || Objects.isNull(item.getStatus())
                || Objects.isNull(item.getPortSpeed()) || Objects.isNull(item.getPortInSpeed()) || Objects.isNull(item.getPortOutSpeed())
                || StrUtil.isEmpty(item.getLosePacketsOutRate()) || StrUtil.isEmpty(item.getLosePacketsInRate())
                || StrUtil.isEmpty(item.getErroCodeOutRate()) || StrUtil.isEmpty(item.getErroCodeInRate()));
        }

}
