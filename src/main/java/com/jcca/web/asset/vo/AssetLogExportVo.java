package com.jcca.web.asset.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author GodWone
 * @description 设备日志导出字段
 * @className AssetLogExportVo
 * @date 2023/2/27 18:29
 * @since 2.0.0.1
 */
@Data
public class AssetLogExportVo {
    private Date createTime;

    private String eventMsg;

    private String uniqueCode;
}
