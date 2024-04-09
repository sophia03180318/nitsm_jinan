package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Integer getServiceType() {
        return serviceType;
    }

    public void setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "SysModuleConfig{" +
                "id=" + id +
                ", name=" + name +
                ", value=" + value +
                ", description=" + description +
                ", createTime=" + createTime +
                ", creator=" + creator +
                ", serviceType=" + serviceType +
                ", orgId=" + orgId +
                "}";
    }
}
