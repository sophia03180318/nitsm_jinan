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
@TableName("TOPO_VERTEX")
public class TopoVertex extends Model<TopoVertex> implements java.io.Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 对应节点ID
     */
    @TableField("NODE_ID")
    private String nodeId;
    /**
     * 节点名称
     */
    @TableField("NAME")
    private String name;
    /**
     * 节点类型
     */
    @TableField("NODE_TYPE")
    private String nodeType;
    /**
     * 节点样式
     */
    @TableField("NODE_STYLE")
    private String nodeStyle;
    /**
     * 节点对应父节点
     */
    @TableField("NODE_PARENT")
    private String nodeParent;
    /**
     * x的坐标位置
     */
    @TableField("NODE_X")
    private Double nodeX;
    /**
     * Y的坐标位置
     */
    @TableField("NODE_Y")
    private Double nodeY;
    /**
     * 节点的宽度
     */
    @TableField("NODE_WIDTH")
    private Integer nodeWidth;
    /**
     * 节点的高度
     */
    @TableField("NODE_HEIGHT")
    private Integer nodeHeight;
    /**
     * 是否允许连接（0:不允许，1：允许）
     */
    @TableField("NODE_CONNECTABLE")
    private Integer nodeConnectable;
    /**
     * 组织结构ID
     */
    @TableField("ORG_ID")
    private String orgId;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * X轴偏移量
     */
    @TableField("OFFSET_X")
    private Integer offsetX;
    /**
     * Y轴偏移量
     */
    @TableField("OFFSET_Y")
    private Integer offsetY;
    /**
     * 是否为端口节点
     */
    @TableField("IS_PORT")
    private Integer isPort;
    /**
     * 端口索引号
     */
    @TableField("PORT_INDEX")
    private String portIndex;

    /**
     * 核心资产Id
     */
    @TableField("CORE_ASSET_ID")
    private String coreAssetId;
}
