package com.jcca.component.thresholds.bean;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * vlan采集数据
 *
 * @author Lvyp
 */
@Data
public class CollectVlanBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 采集时间
     */
    @NotEmpty(message = "采集时间空")
    private String collectTime;
    /**
     * 资产ID
     */
    @NotEmpty(message = "资产ID空")
    private String assetId;
    /**
     * 索引号
     */
    @NotEmpty(message = "索引号空")
    private String vlanIndex;
    /**
     * 名称
     */
    @NotEmpty(message = "vlan名称空")
    private String vlanName;
    /**
     * 类型
     */
    @NotEmpty(message = "vlan类型空")
    private String vlanType;
    /**
     * vlan状态
     */
    @NotEmpty(message = "vlan状态空")
    private String vlanStatus;

}
