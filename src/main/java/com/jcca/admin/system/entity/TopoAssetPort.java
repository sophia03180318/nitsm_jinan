package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

/**
 * 网络端口配置拓扑表
 *
 * @author hanwone
 * @date 2020-08-18 13:53:51
 **/
@Data
@TableName("topo_asset_port")
public class TopoAssetPort extends Model<TopoAssetPort> implements java.io.Serializable {

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
     * 对应端口索引
     */
    @TableField("PORT_INDEX")
    private String portIndex;
    /**
     * 对应vlanID
     */
    @TableField("VLAN_ID")
    private String vlanId;
    /**
     * 节点ID
     */
    @TableField("NODE_ID")
    private String NodeId;
    /**
     * 父节点ID
     */
    @TableField("PARENT_ID")
    private String parentId;
    /**
     * 端口状态，0不正常，1正常
     */
    private Integer status;

    @TableField("UPDATE_DATE")
    private Date updateDate;

    /**
     * 旋转角度
     * syt 2021/9/28
     */
    @TableField("ROTATION")
    private Integer rotation;

    /**
     * 端口模式  光口/电口
     * */
    @TableField("PORT_MODE")
    private String portMode;
    /**
     * 板卡名称
     */
    @TableField("PCB_ID")
    private String pcbId;

    @TableField("PORT_SORT_TYPE")
    private Integer portSortType;
}