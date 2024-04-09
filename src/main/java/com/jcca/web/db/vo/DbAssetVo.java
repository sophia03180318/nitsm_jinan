package com.jcca.web.db.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 资产列表
 *
 * @author Lvyp
 */
@Data
public class DbAssetVo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 资产ID
     */
    private String id;
    /**
     * 资产IP
     */
    private String ip;
    /**
     * 资产别名
     */
    private String name;

}
