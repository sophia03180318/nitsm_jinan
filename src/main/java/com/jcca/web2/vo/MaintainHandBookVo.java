package com.jcca.web2.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @description: 维护手册
 * @author: sophia
 * @create: 2023/11/16 11:34
 **/
@Data
public class MaintainHandBookVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private String id;

    /**
     * 标题
     */
    private String title;

    /**
     * 位置
     */
    private String index;

    private String remark;


}