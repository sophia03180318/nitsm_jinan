package com.jcca.web.graph.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 网络拓扑节点组
 *
 * @author hanwone
 * @date 2020-09-01 19:27:08
 **/
@Data
@TableName("topo_asset_mark")
public class TopoAssetMark extends Model<TopoAssetMark> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 资产ID
     */
    @TableField("ORG_ID")
    private String orgId;
    /**
     * 对应内容
     */
    @TableField("CONTENT")
    private byte[] content;
    /**
     * 节点ID
     */
    @TableField("NODE_TYPE")
    private String nodeType;

    @TableField(exist = false)
    private String contentStr;

    @TableField(exist = false)
    private String assetCode;


    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;

}