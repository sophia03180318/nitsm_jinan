package com.jcca.web2.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: hhw
 * @description: InspectAssetAndTarget 主要是用来
 * @date: 2025-05-21  09:19
 * @since: 2.1.6.0
 */
@Data
public class InspectAssetAndTarget {

    private List<String> assetList;

    private List<ItemVo> targetList;
}
