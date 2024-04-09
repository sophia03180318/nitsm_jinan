package com.jcca.web.common.service.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 告警文件
 *
 * @author sophia
 */
@Data
public class PullAlertLogResultReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ip
     */
    @NotEmpty(message = "设备IP不可空")
    private String ip;

    /**
     * 用户名
     */
    @NotEmpty(message = "用户名不可空")
    private String userName;

    /**
     * 密码
     */
    @NotEmpty(message = "密码不可空")
    private String password;

    /**
     * 端口
     */
    @NotEmpty(message = "端口不可空")
    private Integer port;

    /**
     * 命令
     */
    @NotEmpty(message = "命令不可空")
    private String command;


}
