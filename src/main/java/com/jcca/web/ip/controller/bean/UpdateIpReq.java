package com.jcca.web.ip.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 更新IP信息
 *
 * @author Lvyp
 */
@Data
public class UpdateIpReq implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * ID
     */
    @NotEmpty(message = "请输入ID")
    private String id;
    /**
     * 备注信息
     */
    private String remark;
    /**
     * 审核状态
     */
    private Integer authStatus;
}
