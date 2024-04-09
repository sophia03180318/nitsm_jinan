package com.jcca.web2.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author HanHW
 * @description 指标验证参数
 * @className TargetVerifyVo
 * @date 2024/2/29 14:07
 * @since 2.1.0.0
 */
@Data
public class TargetVerifyVo {

    /**
     * 要测试的设备IP
     */
    @NotEmpty(message = "测试IP不能为空")
    private String ip;
    /**
     * 要测试的端口
     */
    private Integer port;
    /**
     * 用户名或团体名
     */
    @NotEmpty(message = "用户名或团体名不能为空")
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 特权密码
     */
    private String enablePassword;
    /**
     * 测试命令或MIB号
     */
    @NotEmpty(message = "测试命令或MIB号不能为空")
    private String command;
    /**
     * 测试类型
     */
    private VerifyType systemType;

    public enum VerifyType {
        SSH, SNMP, TELNET
    }
}
