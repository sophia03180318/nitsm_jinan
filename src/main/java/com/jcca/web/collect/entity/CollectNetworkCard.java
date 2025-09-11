package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.jcca.common.annotation.WebField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 采集网卡信息
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_NETWORK_CARD")
public class CollectNetworkCard extends Model<CollectNetworkCard> {

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
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 系统类型 SystemTypeEnum
     */
    @TableField("SYSTEM_TYPE")
    private Integer systemType;
    /**
     * 网卡名称
     */
    @WebField(title = "网卡名称")
    @TableField("NAME")
    private String name;
    /**
     * 网卡状态 CollectNetCardStatus
     */
    @TableField("STATUS")
    private Byte status;
    /**
     * mac地址
     */
    @WebField(title = "MAC地址")
    @TableField("MAC_ADDRESS")
    private String macAddress;
    /**
     * ip地址
     */
    @WebField(title = "IP地址")
    @TableField("IP")
    private String ip;
    /**
     * 端口流出
     */
    @TableField("PORT_OUT")
    private Long portOut;
    /**
     * 端口流入
     */
    @TableField("PORT_IN")
    private Long portIn;
    /**
     * 端口流出率
     */
    @TableField("PORT_OUT_SPEED")
    private Long portOutSpeed;
    /**
     * 端口流入率
     */
    @TableField("PORT_IN_SPEED")
    private Long portInSpeed;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(exist = false)
    private Boolean show;

    @Override
    public String toString() {
        return name + "_ASSETID_" + assetId + "_MAC_" + macAddress;
    }

    /**
     * 状态翻译后的文字
     *
     */
    @WebField(title = "状态")
    @TableField(exist = false)
    private String statusStr;
}
