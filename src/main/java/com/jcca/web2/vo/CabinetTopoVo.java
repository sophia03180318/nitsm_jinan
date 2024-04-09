package com.jcca.web2.vo;

import lombok.Data;

/**
 * @description: 中心机柜拓扑信息
 * @author: sophia
 * @create: 2023/10/24 14:48
 **/
@Data
public class CabinetTopoVo {
    private static final long serialVersionUID = 1L;
    /**
     * 机房ID
     * */
    private String roomId;
    /**
     * 机房名称
     * */
    private String roomName;
    /**
     * 机柜名称
     * */
    private String cabinetId;
    /**
     * 机柜ID
     * */
    private String cabinetName;
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
     * 告警状态
     */
    private int alarmLevel;


}