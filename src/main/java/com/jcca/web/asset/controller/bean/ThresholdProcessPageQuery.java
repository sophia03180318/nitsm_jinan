package com.jcca.web.asset.controller.bean;

import com.jcca.common.bean.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页查询
 *
 * @author Lvyp
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ThresholdProcessPageQuery extends PageQuery {

    private static final long serialVersionUID = 1L;
    /**
     * 资产IP
     */
    private String assetIp;
    /**
     * 资产名称
     */
    private String assetName;
    /**
     * 资产编号
     */
    private String assetCode;
    /**
     * 采集状态
     * 1正常 0不正常
     */
    private Byte collectStatus;
    /**
     * 进程名称
     */
    private String processName;
    /**
     * 组织ID
     */
    private String orgId;
    /**
     * 点击出节点的类型
     * 1-4是组织 参考：OrgTypeConst.java
     */
    private Integer type;

}
