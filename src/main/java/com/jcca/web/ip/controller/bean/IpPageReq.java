package com.jcca.web.ip.controller.bean;

import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ip查询
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IpPageReq extends PageQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 网络id
     */
    private String netWorkId;
    /**
     * ip
     */
    private String ip;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 审批状态
     */
    private String authStatus;
    /**
     * 占用状态
     */
    private String pingStatus;

}
