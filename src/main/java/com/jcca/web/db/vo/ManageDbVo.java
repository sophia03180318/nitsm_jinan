package com.jcca.web.db.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据库管理
 *
 * @author Lvyp
 */
@Data
public class ManageDbVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产别名
     */
    private String aliasStr;
    /**
     * 数据库类型 DBTypeEnum
     */
    private Byte dbType;
    /**
     * 数据库类型
     */
    private String dbTypeStr;
    /**
     * 数据库协议 DBTypeEnum
     */
    private Byte dbProtocol;
    /**
     * 数据库协议
     */
    private String dbProtocolStr;
    /**
     * ip
     */
    private String ip;
    /**
     * 端口
     */
    private Integer port;
    /**
     * 数据库名称
     */
    private String name;
    /**
     * 数据库实例名称
     */
    private String dbName;
    /**
     * 表空间阈值
     */
    private Integer tablespaceThreshold;

    private String password;

    private String username;

}
