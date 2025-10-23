package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @description: 表空间
 * @author: Lvyp
 * @create: 2023/11/03 18:58
 */
@Data
public class CollectTablespaceEntity extends CommonEntity implements Serializable {


    /**
     * 数据ID
     */
    private String id;
    /**
     * 表空间名称
     */
    private String name;
    /**
     * 状态
     */
    private String status;
    /**
     * 表空间总大小M
     */
    private Long totalSize;
    /**
     * 空闲大小M
     */
    private Long freeSize;
    /**
     * 使用大小M
     */
    private Long usedSize;
    /**
     * 使用率%
     */
    private Double usedRate;
    /**
     * 表空间最大扩展大小 M
     */
    private Long maxSize;

    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * collect_db主键ID
     */
    private String collectDbId;

}
