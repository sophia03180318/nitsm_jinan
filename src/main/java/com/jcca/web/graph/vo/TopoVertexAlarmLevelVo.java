package com.jcca.web.graph.vo;

import lombok.Data;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 15:34
 */
@Data
public class TopoVertexAlarmLevelVo implements java.io.Serializable {
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
     * 如果是集群的话会有这个表示当前设备
     * 是主节点还是从节点
     */
    private String serverRole;
    /**
     * 集群状态下服务IP
     */
    private String serverIp;
    /**
     * 集群状态
     */
    private String serverIp2;
    /**
     * 集群状态
     */
    private String clusterStatus;
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
     * 资产Id
     */
    private String assetId;
    /**
     * 资产型号
     */
    private String assetImage;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 展示核心
     */
    private String showCore;
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
     * 告警状态
     */
    private int alarmLevel;

    /**
     * 端口索引
     */
    private String portIndex;

    /**
     * 横向位置
     */
    private Integer rowIndex;

    /**
     * 列位置
     */
    private Integer columnIndex;

    /**
     * 是否可以下钻  true可以下钻    false不可下钻
     */
    private Boolean isDown;
    /**
     * 是否监控，0-不监控，1-监控
     */
    private Integer watch;

    /**
     * 时间偏差
     * */
    private String systemTime;

    /**
     * ab机配置
     * AB机标识（0=A机,1=B机，2=集群）
     */
    private Byte aBFlag;

}
