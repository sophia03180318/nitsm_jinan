package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description: 交换机板卡配置
 * @author: Lvyp
 * @create: 2024/01/10 11:54
 */
@Data
@TableName("TOPO_PCB")
public class TopoPcb {

    /**
     * id
     */
    @TableId(value = "PCB_ID", type = IdType.ID_WORKER_STR)
    private String pcbId;
    /**
     * 资产ID
     */
    @TableField("ASSET_ID")
    private String assetId;
    /**
     * 名称
     */
    @TableField("PCB_NAME")
    private String pcbName;
    /**
     * 排序
     */
    @TableField("PCB_SORT")
    private Integer pcbSort;

    @TableField(exist = false)
    private Boolean haveTopo;
}
