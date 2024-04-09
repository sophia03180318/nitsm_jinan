package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 硬件端口
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_INTERFACES")
public class CollectInterfaces extends Model<CollectInterfaces> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 采集时间
     */
    @TableField("COLLECT_TIME")
    private Date collectTime;
    /**
     * 采集编号 同一台设备同一次采集编号相同
     */
    @TableField("COLLECT_CODE")
    private String collectCode;
    /**
     * 资产主键
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 端口名称
     */
    @TableField("PORT_NAME")
    private String portName;
    /**
     * 端口别名
     */
    @TableField("PORT_ALIAS")
    private String portAlias;
    /**
     * 端口类型
     */
    @TableField("PORT_TYPE")
    private Integer portType;
    /**
     * 端口索引
     */
    @TableField("PORT_INDEX")
    private String portIndex;
    /**
     * 端口索引排序
     */
    @TableField("PORT_INDEX_RANK")
    private Integer portIndexRank;
    /**
     * 端口链接类型
     */
    @TableField("PORT_LINK_TYPE")
    private Integer portLinkType;
    /**
     * 端口对应的IP地址
     */
    @TableField("LINK_IP")
    private String linkIp;
    /**
     * 端口对应的mask地址
     */
    @TableField("LINK_MASK")
    private String linkMask;
    /**
     * 端口对应的phy地址
     */
    @TableField("LINK_PHY_ADDRESS")
    private String linkPhyAddress;

    /**
     * 端口流入 b
     */
    @TableField("PORT_IN")
    private Long portIn;

    @TableField("PORT_IN_COUNT")
    private Long portInCount;
    /**
     * 端口流出 b
     */
    @TableField("PORT_OUT")
    private Long portOut;

    @TableField("PORT_OUT_COUNT")
    private Long portOutCount;
    /**
     * 端口流入丢包数
     */
    @TableField("DISCARD_PACKETS_IN")
    private Long discardPacketsIn;
    /**
     * 流入总丢包
     */
    @TableField("DISCARD_PACKETS_IN_ALL")
    private Long discardPacketsInCount = 0L;
    /**
     * 端口流出丢包数
     */
    @TableField("DISCARD_PACKETS_OUT")
    private Long discardPacketsOut;
    /**
     * 端口流出总丢包
     */
    @TableField("DISCARD_PACKETS_OUT_ALL")
    private Long discardPacketsOutCount = 0L;
    /**
     * 非单播流入量 单位：b
     */
    @TableField("NO_UNICAST_PACKETS_IN")
    private Long noUnicastPacketsIn;
    /**
     * 非单播流出量 单位：b
     */
    @TableField("NO_UNICAST_PACKETS_OUT")
    private Long noUnicastPacketsOut;
    /**
     * 单播流入量 单位：b
     */
    @TableField("UNICAST_PACKETS_IN")
    private Long unicastPacketsIn;
    /**
     * 单播流出量 单位：b
     */
    @TableField("UNICAST_PACKETS_OUT")
    private Long unicastPacketsOut;
    /**
     * 误码流入量 单位：b
     */
    @TableField("ERROR_CODE_IN")
    private Long errorCodeIn;
    /**
     * 误码总流入
     */
    @TableField("ERROR_CODE_IN_ALL")
    private Long errorCodeInCount = 0L;
    /**
     * 误码流出量 单位：b
     */
    @TableField("ERROR_CODE_OUT")
    private Long errorCodeOut;
    /**
     * 误码总流出
     */
    @TableField("ERROR_CODE_OUT_ALL")
    private Long errorCodeOutCount = 0L;
    /**
     * 端口状态 InterfaceStatus
     */
    @TableField("STATUS")
    private Byte status;
    /**
     * 采集状态 CollectInterfaceStatus
     */
    private Byte collectStatus;
    /**
     * 端口允许的最大速率 b/s
     */
    @TableField("PORT_SPEED")
    private Long portSpeed;
    /**
     * 端口接收速率 b/s
     */
    @TableField("PORT_IN_SPEED")
    private Long portInSpeed;
    /**
     * 端口发送速率 b/s
     */
    @TableField("PORT_OUT_SPEED")
    private Long portOutSpeed;
    /**
     * 端口发送丢包率 百分比-数字格式
     */
    @TableField("LOSE_PACKETS_OUT_RATE")
    private Double losePacketsOutRate;
    /**
     * 端口接收丢包率 百分比-数字格式
     */
    @TableField("LOSE_PACKETS_IN_RATE")
    private Double losePacketsInRate;
    /**
     * 端口发送错误率 百分比-数字格式
     */
    @TableField("ERRO_CODE_OUT_RATE")
    private Double erroCodeOutRate;
    /**
     * 传输功率 dBm
     */
    @TableField("TX_POWER")
    private String txPower;
    /**
     * 接收功率 dBm
     */
    @TableField("RX_POWER")
    private String rxPower;
    /**
     * 端口接收错误率 百分比-数字格式
     */
    @TableField("ERRO_CODE_IN_RATE")
    private Double erroCodeInRate;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
    /**
     * CRC校验错误数
     */
    @TableField("CRC_ERRORS")
    private Long crcErrors;



    /**
     * 分组标识
     */
    @TableField(exist = false)
    private String groupFlg;

}
