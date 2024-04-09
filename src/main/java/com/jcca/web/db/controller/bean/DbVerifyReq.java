package com.jcca.web.db.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 验证数据库
 *
 * @author Lvyp
 */
@Data
public class DbVerifyReq implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 数据库IP
     */
    @NotEmpty(message = "IP不可空")
    private String ip;
    /**
     * 数据库端口
     */
    @NotNull(message = "端口不能空")
    private Integer port;
    /**
     * 数据库用户名
     */
    @NotEmpty(message = "用户名不能空")
    private String username;
    /**
     * 密码
     */
    @NotEmpty(message = "密码不能空")
    private String password;
    /**
     * 数据库实例名称
     */
    @NotEmpty(message = "数据库实例名称不能空")
    private String dbName;
    /**
     * 数据库类型
     */
    @NotNull(message = "数据库类型不能空")
    private Byte dbType;
}
