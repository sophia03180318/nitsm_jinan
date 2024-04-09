package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 网络端口配置中面板的信息
 *
 * @author syt
 * @date 2021/07/15  16:15
 * @classname nitsmcom.jcca.admin.system.entityTopoAssetPortPic
 */
@Data
@TableName("topo_asset_port_pic")
public class TopoAssetPortPic extends Model<TopoAssetPortPic> implements java.io.Serializable {
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

    /**
     * 对应内容
     */
    @TableField(exist = false)
    private String contentStr;

    /**
     * 面板型号
     */
    @TableField("CATEGORY_PIC")
    private String categoryPic;

    /**
     * 面板图节点ID
     */
    @TableField("NODE_ID")
    private String nodeId;

    /**
     * 面板图 图片名称
     */
    @TableField("PICTURE_IMG")
    private String pictureImg;

    /**
     * 面板图位置 X
     */
    @TableField("POINT_X")
    private Integer pointX;

    /**
     * 面板图位置 Y
     */
    @TableField("POINT_Y")
    private Integer pointY;

    /**
     * 面板图大小 高
     */
    @TableField("HEIGHT")
    private Integer height;

    /**
     * 面板图大小 宽
     */
    @TableField("WIDTH")
    private Integer width;
    /**
     * 板卡名称
     */
    @TableField("PCB_ID")
    private String pcbId;


}
