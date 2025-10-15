package com.jcca.web2.vo;


import lombok.Data;

import java.util.List;

/**
 * @author lifp
 * @version 1.0
 * @description: 通用组织机构实体
 * @date 2025-10-09 星期四 11:25:28`
 */
@Data
public class CommonOrganizationInfo {

    // 组织ID
    private String id;

    // 名称
    private String orgName;

    // 父级iD
    private String parentId;

    List<CommonAssetInfo> children;
}
