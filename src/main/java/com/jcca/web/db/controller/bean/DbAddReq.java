package com.jcca.web.db.controller.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 数据库配置信息
 *
 * @author Lvyp
 */
@Data
public class DbAddReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产ID
     */
    @NotEmpty(message = "资产ID不能空")
    private String assetId;
    /**
     * 数据库类型 DBTypeEnum
     */
    @NotNull(message = "数据类型不能空")
    private Byte dbType;
    /**
     * 数据库协议 DBTypeEnum
     */
    @NotNull(message = "数据库协议不能空")
    private Integer dbProtocol;
    /**
     * 端口
     */
    @NotNull(message = "端口不能空")
    private Integer port;
    /**
     * 账号
     */
    @NotEmpty(message = "账号不能空")
    private String username;
    /**
     * 密码
     */
    @NotEmpty(message = "密码不能空")
    private String password;
    /**
     * 数据库实例名称
     */
    @NotEmpty(message = "数据库实例名不能空")
    private String dbName;
    /**
     * 数据库名称
     */
    @NotEmpty(message = "数据库名不能空")
    private String name;


    @NotNull(message = "请选择厂商")
    private Integer manufacturerId;

    @NotNull(message = "请选择资产型号")
    private String assetImage;
}
