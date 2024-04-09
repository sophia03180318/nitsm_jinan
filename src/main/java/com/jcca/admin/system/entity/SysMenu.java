package com.jcca.admin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 菜单表
 *
 * @author hanwone
 * @date 2020-04-06 12:14:51
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_menu")
public class SysMenu extends Model<SysMenu> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 菜单ID
     */
    @TableId(value = "id", type = IdType.ID_WORKER_STR)
    private String id;
    /**
     * 菜单名称
     */
    @TableField("TITLE")
    @NotEmpty(message = "标题不能为空")
    @Length(max = 20, message = "内容不能超过20个汉字")
    private String title;
    /**
     * 父级ID
     */
    @TableField("PID")
    @NotNull(message = "父级菜单不能为空")
    private String pid;
    /**
     * 所有父级ID
     */
    @TableField("PIDS")
    private String pids;
    /**
     * 菜单URL
     */
    @TableField("URL")
    @NotEmpty(message = "url地址不能直接为空，可以输入#代替！")
    private String url;
    /**
     * 菜单权限
     */
    @TableField("PERMS")
    @NotEmpty(message = "权限标识不能直接为空，可以输入#代替！")
    private String perms;
    /**
     * 菜单图标
     */
    @TableField("ICON")
    private String icon;
    /**
     * 菜单类型
     */
    @TableField("TYPE")
    @NotNull(message = "菜单类型不能为空")
    private Byte type;
    @TableField(exist = false)
    private String typeStr;
    /**
     * 排序
     */
    @TableField("SORT")
    private Byte sort;
    /**
     * 状态
     */
    @TableField("STATUS")
    private Byte status;
    /**
     * 备注
     */
    @TableField("REMARK")
    @Length(max = 42, message = "内容不能超过42个汉字")
    private String remark;
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

    @JsonIgnore
    @TableField(exist = false)
    private Map<Long, SysMenu> children = new HashMap<>();

    public SysMenu() {

    }

    public SysMenu(String id, String title, String pids) {
        this.id = id;
        this.title = title;
        this.pids = pids;
    }

    public void setPids(String pids) {
        if (pids.startsWith(",")) {
            pids = pids.substring(1);
        }
        this.pids = pids;
    }


    public String toData() {
        return url + ",," + title;
    }
}