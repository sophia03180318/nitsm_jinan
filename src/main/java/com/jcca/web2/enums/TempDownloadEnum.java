package com.jcca.web2.enums;

import lombok.Getter;

/**
 * @author HanHW
 * @description 表格模板统一下载
 * @className TempDownloadEnum
 * @date 2023/12/1 10:37
 * @since 2.1.0.0
 */
@Getter
public enum TempDownloadEnum {

    // 资产导入模板
    ASSET_IMPORT(1, "asset_import_template.xlsx"),

    ;

    int code;

    String fileName;

    TempDownloadEnum(Integer code, String fileName) {
        this.code = code;
        this.fileName = fileName;
    }

    public static String getFileNameByCode(Integer code) {
        TempDownloadEnum[] values = TempDownloadEnum.values();
        for (TempDownloadEnum value : values) {
            if (value.code == code) {
                return value.fileName;
            }
        }
        return "";
    }
}
