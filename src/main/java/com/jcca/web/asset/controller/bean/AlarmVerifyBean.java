package com.jcca.web.asset.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 告警校验测试配置
 *
 * @author lyp
 */
@Data
public class AlarmVerifyBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    /**
     * ping 次数
     */
    @NotNull(message = "请输入ping轮询次数")
    private Integer pingSize;
    /**
     * 进程状态 次数
     */
    @NotNull(message = "请输入进程状态轮询次数")
    private Integer processSize;

}
