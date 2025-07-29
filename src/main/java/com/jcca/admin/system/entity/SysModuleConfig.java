package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 系统功能模块参数设置
 * 配置覆盖规则:
 * 1.相同配置项,SERVICE_TYPE 为0为 1级配置
 * 2.相同配置项,SERVICE_TYPE 不为0 为2级配置
 * 3.相同配置项,SERVICE_TYPE不为0并且ORG_ID不为0为3级配置
 * <p>
 * 相同配置项大级别覆盖小级别
 *
 * </p>
 *
 * @author LuBan
 * @since 2021-01-05
 */
@Data
@TableName("SYS_MODULE_CONFIG")
public class SysModuleConfig extends Model<SysModuleConfig> {

    private static final long serialVersionUID = 1L;

    @TableId("ID")
    private String id;

    /**
     * 配置名称
     */
    @TableField("NAME")
    private String name;

    /**
     * 配置值
     */
    @TableField("VALUE")
    private String value;

    /**
     * 配置描述
     */
    @TableField("DESCRIPTION")
    private String description;

    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    private Date createTime;

    /**
     * 创建人
     */
    @TableField("CREATOR")
    private String creator;

    /**
     * 配置适配服务类型: 0全部服务,1:中心采集器,2:车站采集器,3:itsm,4:卡斯柯接口服务
     */
    @TableField("SERVICE_TYPE")
    private Integer serviceType;

    /**
     * 配置适配组织结构
     */
    @TableField("ORG_ID")
    private String orgId;
    /**
     * web界面展示形态配置
     *
     */
    @TableField("web_conf")
    private String webConf;
    /**
     * web界面展示名称
     */
    @TableField("title")
    private String title;

}
