package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description: 夜莺统计图的图表模板
 * @author: Lvyp
 * @create: 2023/11/10 16:42
 */
@TableName("NIGHT_BOARD")
@Data
public class NightBoard extends Model<NightBoard> {

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 配置的名字
     */
    @TableField("NAME")
    private String name;
    /**
     *
     */
    @TableField("IDENT")
    private String ident;
    /**
     *
     */
    @TableField("TAGS")
    private String tags;
    /**
     * 0:false 1:true
     */
    @TableField("PUB")
    private Integer pub;
    /**
     * 0:false 1:true
     */
    @TableField("BUILT_IN")
    private Integer builtIn;
    /**
     * 0:false 1:true
     */
    @TableField("HIDE")
    private Integer hide;
    /**
     * 0:false 1:true
     * 启用
     */
    @TableField("ENABLE")
    private Integer enable;
    /**
     * 资产类型
     */
    @TableField("ASSET_MODE")
    private Integer assetMode;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField("CREATE_AT")
    private Date createAt;
    /**
     * 修改时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField("UPDATE_AT")
    private Date updateAt;

}
