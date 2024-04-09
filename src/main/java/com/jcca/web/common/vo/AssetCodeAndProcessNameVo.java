package com.jcca.web.common.vo;

import lombok.Data;

/**
 * @author syt
 * @date 2021/08/27  10:05
 * @classname nitsmcom.jcca.web.common.voAssetCodeAndProcessNameVo
 */
@Data
public class AssetCodeAndProcessNameVo {
    private String assetCode;
    private String processName;
    /**
     * 模式 1双击单活 2双机双活 3普通
     */
    private Integer hostMode;
    private String ip;
}
