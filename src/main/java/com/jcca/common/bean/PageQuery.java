package com.jcca.common.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询公共
 *
 * @author lyp
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 起始页
     */
    private Integer page;
    /**
     * 页大小
     */
    private Integer size;

}
