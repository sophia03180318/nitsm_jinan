package com.jcca.admin.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 车站配置表
 *
 * @author hanwone
 * @date 2020-06-19 11:08:31
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("station")
public class Station extends Model<Station> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 车站ID,对应组织表车站ID
     */
    @TableId(value = "ORG_ID", type = IdType.INPUT)
    @NotBlank(message = "组织ID为空")
    private String orgId;
    /**
     * 车站名称
     */
    @TableField("TITLE")
    private String title;
    /**
     * IP
     */
    @TableField("IP")
    @NotBlank(message = "车站IP为空")
    private String ip;
    /**
     * IP2
     */
    @TableField("IP2")
    private String ip2;
    /**
     * 端口
     */
    @TableField("PORT")
    @NotNull(message = "端口为空")
    private Integer port;
    /**
     * 车站运行状态
     */
    @TableField("RUN_STATUS")
    private String runStatus;
    /**
     * 车站当前的版本
     */
    @TableField("TARGET_NAME")
    private String targetName;
    /**
     * 备注
     */
    @TableField("REMARK")
    private String remark;
    /**
     * 车站jar部署文件位置
     */
    @TableField("FILE_PATH")
    private String filePath;
    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", fill = FieldFill.INSERT)
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
    private Date modifyTime;
    /**
     * 修改者
     */
    @TableField(value = "MODIFIER", fill = FieldFill.INSERT_UPDATE)
    private String modifier;

    @TableField(value = "STATION_GROUP", fill = FieldFill.INSERT_UPDATE)
    @NotNull(message = "车站分组为空")
    private Integer stationGroup;

    /**
     * 页面所筛选的组织树回传id 与机柜无直接关系
     */
    @TableField(exist = false)
    private String orgTreeId;
    /**
     * 访问车站采集器代理URL
     */
    @TableField("PROXY_URL")
    private String proxyUrl;

}