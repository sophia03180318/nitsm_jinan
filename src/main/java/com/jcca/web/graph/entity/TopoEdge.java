package com.jcca.web.graph.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 15:34
 */
@Data
@TableName("TOPO_EDGE")
public class TopoEdge extends Model<TopoEdge> implements java.io.Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 对应的节点ID
     */
    @TableField("EDGE_ID")
    private String edgeId;

    /**
     * 节点类型
     */
    @TableField("EDGE_TYPE")
    private String edgeType;
    /**
     * 节点样式
     */
    @TableField("EDGE_STYLE")
    private String edgeStyle;
    /**
     * 节点对应父节点
     */
    @TableField("EDGE_PARENT")
    private String edgeParent;

    /**
     * 开始节点
     */
    @TableField("EDGE_SOURCE")
    private String edgeSource;
    /**
     * 目标节点
     */
    @TableField("EDGE_TARGET")
    private String edgeTarget;
    /**
     * 组织结构ID
     */
    @TableField("ORG_ID")
    private String orgId;

    //初始连接方向
    @TableField("SOURCE_POSITION")
    private Integer sourcePosition;

    /**
     * 对应的资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;


    /**
     * 端口类型
     * */
    @TableField("PORT_MODE")
    private String portMode;

}
