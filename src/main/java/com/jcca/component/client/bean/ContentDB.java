package com.jcca.component.client.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 验证DB需要的信息
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContentDB extends ContentBean {

    private static final long serialVersionUID = 1L;
    /**
     * 数据库Ip
     */
    private String ip;
    /**
     * 端口号
     */
    private Integer port;
    /**
     * 数据库类型
     */
    private Byte dbType;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String pwd;
    /**
     * 实例名
     */
    private String name;

}
