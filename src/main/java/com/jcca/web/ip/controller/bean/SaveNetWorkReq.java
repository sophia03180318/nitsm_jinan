package com.jcca.web.ip.controller.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 新建网络
 *
 * @author lyp
 */
@Data
public class SaveNetWorkReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    @ApiModelProperty(hidden = true, required = false)
    private String id;

    /**
     * 网络名称
     */
    @NotEmpty(message = "请输入网络名称")
    @Length(max = 60, message = "名称不能超过60个字符")
    private String name;
    /**
     * 网络地址
     */
    private String netIp;
    /**
     * 掩码位数
     */
    private Integer maskNumber;


    /**
     * 起始ip
     */
    private String startIp;

    /**
     * 结束ip
     */
    private String endIp;

    /**
     * 掩码
     */
    @NotNull(message = "请输入掩码")
    @Pattern(regexp = "^((128|192)|2(24|4[08]|5[245]))(\\.(0|(128|192)|2((24)|(4[08])|(5[245])))){3}$", message = "掩码格式不正确")
    private String mask;


    /**
     * 网关地址
     */
    @NotEmpty(message = "请输入网关地址")
    @Pattern(regexp = "^([1-9]|[1-9]\\d|1\\d{2}|2[0-1]\\d|22[0-3])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$", message = "网关地址格式不正确")
    private String gateway;

    /**
     * 备注
     */
    @Length(max = 60, message = "备注不能超过60个字符")
    private String remark;

    @NotEmpty(message = "请为网段分配组织")
    private String orgId;
}
