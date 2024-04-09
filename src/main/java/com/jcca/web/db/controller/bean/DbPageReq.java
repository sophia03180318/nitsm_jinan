package com.jcca.web.db.controller.bean;

import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据库配置分页查询
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DbPageReq extends PageQuery {

    private static final long serialVersionUID = 1L;
    /**
     * 名称
     */
    private String name;
    /**
     * ip
     */
    private String ip;


}
