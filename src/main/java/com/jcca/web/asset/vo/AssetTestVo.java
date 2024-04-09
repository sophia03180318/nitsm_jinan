package com.jcca.web.asset.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @ClassName AssetTestVo
 * @Description 资产测试
 * @Date 2020/5/22 17:46
 * @Author hanwone
 */
@Data
public class AssetTestVo {

    @NotNull(message = "请输入IP")
    @Pattern(message = "ip地址格式不正确", regexp = "^((25[0-5]|2[0-4]\\d|[1]\\d\\d|[1-9]\\d|\\d)($|(?!\\.$)\\.)){4}$")
    private String ip;
    @NotNull(message = "请输入操作用户名")
    private String osUser;
    @NotNull(message = "请输入操作密码")
    private String osPassword;
    @NotNull(message = "请输入采集协议")
    private Byte protocolType;
}
