package com.jcca.web.graph.vo;

import lombok.Data;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 15:34
 */
@Data
public class TopoVertexVo implements java.io.Serializable {

    public static final Integer FIND_OK = 1;
    public static final Integer FIND_NO = 0;

    /**
     * 主键ID
     */
    private String id;
    /**
     * 对应节点ID
     */
    private String nodeId;
    /**
     * 节点名称
     */
    private String name;
    /**
     * 节点类型
     */
    private String nodeType;
    /**
     * 节点样式
     */
    private String nodeStyle;
    /**
     * 节点对应父节点
     */
    private String nodeParent;
    /**
     * x的坐标位置
     */
    private Double nodeX;
    /**
     * Y的坐标位置
     */
    private Double nodeY;
    /**
     * 节点的宽度
     */
    private Integer nodeWidth;
    /**
     * 节点的高度
     */
    private Integer nodeHeight;
    /**
     * 是否允许连接（0:不允许，1：允许）
     */
    private Integer nodeConnectable;
    /**
     * 组织结构ID
     */
    private String orgId;
    /**
     * 机房ID
     */
    private String roomId;

    /**
     * 资产Id
     */
    private String assetId;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * ab机配置
     * AB机标识（0=A机,1=B机，2=集群）
     */
    private Byte aBFlag;
    /**
     * 资产类型
     */
    private String assetMode;

    /**
     * 资产对应orgId
     */
    private String assetOrgId;

    /**
     * X轴偏移量
     */
    private Integer offsetX;
    /**
     * Y轴偏移量
     */
    private Integer offsetY;
    /**
     * 是否为端口
     */
    private Integer isPort;
    /**
     * 端口索引
     */
    private String portIndex;

    /**
     * 横向位置
     */
    private Integer rowIndex;
    /**
     * 纵向位置
     */
    private Integer columnIndex;

    /**
     * topo发现完成标志
     * 1完成0未完成
     */
    private Integer findFinishFlag;

    /**
     * 告警级别
     */
    private Integer alarmLevel;

    /**
     * 是否核心设备  SHOW_TOPO_@_SHOW 核心， SHOW_TOPO_@_NO_SHOW 非核心
     */
    private String showCore;
}
