package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 网络端口Vlan拓扑表
 *
 * @author hanwone
 * @date 2020-08-19 18:14:39
 **/
@Data
@TableName("topo_asset_port_vlan")
public class TopoAssetPortVlan extends Model<TopoAssetPortVlan> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 对应内容
     */
    @TableField("CONTENT")
    private byte[] content;


    @TableField(exist = false)
    private String contentStr;

    /**
     * 板卡ID
     */
    @TableField("PCB_ID")
    private String pcbId;

}