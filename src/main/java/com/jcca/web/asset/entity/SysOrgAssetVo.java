package com.jcca.web.asset.entity;

import com.jcca.admin.system.entity.SysOrg;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 资产组织组织树
 * syt
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class SysOrgAssetVo extends SysOrg implements java.io.Serializable {

    private static final long serialVersionUID = 1L;


    private List<Asset> assets;

}