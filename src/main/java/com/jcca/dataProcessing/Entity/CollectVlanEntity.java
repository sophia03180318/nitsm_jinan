package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.io.Serializable;

/**
 * vlan采集数据
 *
 * @author Lvyp
 */
@Data
public class CollectVlanEntity extends  CommonEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 索引号
     */
    private String vlanIndex;
    /**
     * 名称
     */
    private String vlanName;
    /**
     * 类型
     */
    private String vlanType;
    /**
     * vlan状态
     */
    private String vlanStatus;

}
