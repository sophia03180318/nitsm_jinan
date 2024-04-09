package com.jcca.web.collect.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author hanhw
 * @description 设备连接信息
 * @className AssetLinkAsset
 * @date 2023/4/25 15:57
 * @since 2.0.3.0
 */
@Data
@TableName("ASSET_LINK_ASSET")
public class AssetLinkAsset {

    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 本端资产ID
     */
    private String assetId;
    /**
     * 本端端口
     */
    private String portIndex;
    /**
     * 本端端口IP
     */
    private String portIp;
    /**
     * 接收流量
     */
    private String portIn;
    /**
     * 发送流量
     */
    private String portOut;
    /**
     * 本端端口状态
     */
    private String portStatus;
    /**
     * 本端端口索引
     */
    private String portIndexRank;
    /**
     * 对端设备ID
     */
    private String linkAssetId;
    /**
     * 对端设备名称
     */
    private String linkAssetName;
    /**
     * 对端设备IP
     */
    private String linkAssetIp;
    /**
     * 对端设备端口
     */
    private String linkPort;
    /**
     * 是拓扑图配置的 还是自动发现的
     * 1拓扑图配置的，2自动发现的，3手动录入的
     * 自动发现的对端设备信息可以修改，拓扑图配置的对端设备信息不可以修改
     */
    private String maOrAt;
    /**
     * 自动发现的对端设备ID
     */
    private String atAssetId;
    /**
     * 自动发现的对端设备名称
     */
    private String atName;
    /**
     * 自动发现的对端设备IP
     */
    private String atNetAddress;
    /**
     * 自动发现的对端设备端口
     */
    private String atPortIndexName;
    /**
     * 创建时间
     * */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 创建者
     */
    @TableField(value = "CREATOR", fill = FieldFill.INSERT)
    private String creator;
    /**
     * 修改时间
     */
    @TableField(value = "MODIFY_TIME", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

}
