package com.jcca.web.ip.controller.bean;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 检测ip状态
 *
 * @author lyp
 */
@Data
public class DetectionIpReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "请传入待检测ip的id")
    private List<String> ids;

}
