package com.jcca.web.ip.controller.bean;

import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 网络信息分页查询
 *
 * @author lyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NetWorkPageQuery extends PageQuery {

    private static final long serialVersionUID = 1L;

    private String name;
    private String gateway;
    private String mask;

}
