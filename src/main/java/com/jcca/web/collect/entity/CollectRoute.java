package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;

/**
 * 采集路由信息
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_ROUTE")
public class CollectRoute extends Model<Model<CollectRoute>> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 索引号
     * 匹配端口采集表中的 portIndexRank
     */
    @TableField("PORT_INDEX_RANK")
    private String portIndexRank;
    /**
     * 端口原始名称-vlan名称
     */
    @TableField("PORT_ORG_NAME")
    private String portOrgName;
    /**
     * 端口名称
     */
    @TableField("PORT_NAME")
    private String portName;
    /**
     * 本端端口IP
     */
    @TableField("PORT_IP")
    private String portIp;
    /**
     * 本段端口MAC地址
     */
    @TableField("PORT_MAC_ADDRESS")
    private String portMacAddress;
    /**
     * 端口索引名称
     */
    @TableField("PORT_INDEX_NAME")
    private String portIndexName;
    /**
     * 对端IP
     */
    @TableField("AT_NET_ADDRESS")
    private String atNetAddress;
    /**
     * 对端物理地址
     */
    @TableField("AT_PHYS_ADDRESS")
    private String atPhysAddress;

    /**
     * 对端资产ID
     */
    @TableField("AT_ASSET_ID")
    private String atAssetId;

    /**
     * 对端资产名称
     */
    @TableField(value = "AT_NAME", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private String atName;

    /**
     * 备注
     */
    @TableField(value = "REMARK", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private String remark;

    /**
     * 对端端口名称或网卡名称
     */
    @TableField(value = "AT_PORT_NAME", updateStrategy = FieldStrategy.IGNORED, jdbcType = JdbcType.VARCHAR)
    private String atPortName;

    @TableField("AT_PORT_INDEX_NAME")
    private String atPortIndexName;
    /**
     * 对端设备类型
     */
    @TableField("AT_TYPE")
    private Integer atType;
    /**
     * 对端设备是否已经丢失
     */
    @TableField("IS_LOST")
    private Integer isLost;

    @TableField("CREATE_DATE")
    private Date createDate;
}
