package com.jcca.web2.dto;


import lombok.Data;

import java.util.List;

/**
 * @author lifp
 * @version 1.0
 * @description: 实体接收类
 * @date 2025-10-14 星期二 15:53:18
 */
@Data
public class CommonOrganizationsDto {

    /**
     * 组织id集合
     */
    private List<String> orgIdList;

    /**
     * 资产id集合
     */
    private List<String> assetIdList;
}
