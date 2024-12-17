package com.jcca.web.asset.vo;

import lombok.Data;

/**
 * @author HW
 * @description 对端信息导出
 * @className LinkAssetExportVo
 * @date 2024/12/2 15:36
 * @since 2.1.1.0
 */
@Data
public class LinkAssetExportVo {

    private String assetName;
    private String assetIp;
    private String portIndex;
    private String linkAssetName;
    private String linkAssetIp;
    private String linkPort;
}
