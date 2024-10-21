package com.jcca.web2.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * @description: 端口面板
 * @author: sophia
 * @create: 2023/11/02 14:39
 **/
@Data
@TableName("PORT_TEMPLATE")
public class PortTemp extends Model<PortTemp> {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;

    /**
     * 所属类型ID
     */
    @TableField("MODEL_ID")
    private String modelId;

    /**
    * 各类型端口数量 (json字符串)
    * */
    @TableField("PORT_COUNT")
    private String portCount;

    /**
     * 模板名称
     */
    @TableField("NAME")
    private String name;

    /**
     * 端口排序类型
     * 默认空  空代表上下
     * 1代表横
     */
    @TableField("PORT_SORT_TYPE")
    private Integer portSortType;


    /**
     * 模板内容
     */
    @TableField(exist = false)
    private String msg;



}