package com.jcca.web.common.service.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 获取进程请求信息
 *
 * @author lyp
 */
@Data
public class BusinessGetSnmpResultReq implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String GET = "GET";
    public static final String WALK = "WALK";

    /**
     * ip
     */
    @NotEmpty(message = "设备IP不可空")
    private String ip;
    /**
     * mib
     */
    private String mib;
    /**
     * 团体名
     */
    @NotEmpty(message = "团体名不可空")
    private String community;
    /**
     * GET\WALK
     */
    private String type;


}
