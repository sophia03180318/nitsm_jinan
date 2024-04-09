package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * vlan采集数据
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("COLLECT_VLAN")
public class CollectVlan extends Model<CollectVlan> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
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
     * 索引号
     */
    @TableField("VLAN_INDEX")
    private String vlanIndex;
    /**
     * 名称
     */
    @TableField("VLAN_NAME")
    private String vlanName;
    /**
     * 类型
     */
    @TableField("VLAN_TYPE")
    private String vlanType;
    /**
     * vlan状态
     */
    @TableField("VLAN_STATUS")
    private String vlanStatus;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;
}
