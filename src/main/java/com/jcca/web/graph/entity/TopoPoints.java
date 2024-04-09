package com.jcca.web.graph.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 15:33
 */
@Data
@TableName("TOPO_POINTS")
public class TopoPoints extends Model<TopoPoints> implements java.io.Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 对应连线节点ID
     */
    @TableField("EDGE_ID")
    private String edgeId;
    /**
     * x位置
     */
    @TableField("POINT_X")
    private Integer pointX;
    /**
     * Y位置
     */
    @TableField("POINT_Y")
    private Integer pointY;
    /**
     * 组织结构ID
     */
    @TableField("ORG_ID")
    private String orgId;

    /**
     * 节点类型
     */
    @TableField("EDGE_TYPE")
    private String edgeType;

    /**
     * 对应的资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
}
