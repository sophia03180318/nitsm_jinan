package com.jcca.web.asset.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 设备隐藏配置
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("asset_hid_conf")
public class AssetHidConf extends Model<AssetHidConf> implements java.io.Serializable {

    /**
     * 配置隐藏的类型
     */
    public enum TypeEnum {
        /**
         * 网卡
         */
        NET_CARD,
        /**
         * 端口
         */
        PORT

    }


    /**
     * ID
     */
    @TableId(value = "CONF_ID", type = IdType.NONE)
    private String confId;
    /**
     * 类型
     */
    @TableField("TYPE")
    private String type;
    /**
     * 隐藏的标识
     */
    @TableField("FLAG")
    private String flag;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
    private Date createTime;

}
