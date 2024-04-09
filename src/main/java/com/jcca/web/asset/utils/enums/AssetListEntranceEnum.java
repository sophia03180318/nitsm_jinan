package com.jcca.web.asset.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author syt
 * @date 2021/06/09  14:41
 * @classname nitsmcom.jcca.web.asset.utils.enumsAssetListEntranceEnum
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum AssetListEntranceEnum {
    //1监控列表,  0资产列表, 2导出(非列表显示)
    MONITOR(1, "监控列表"),

    ASSET(0, "资产列表"),

    EXPORT(2, "导出(非列表显示)");
    private int code;
    private String entrance;

}
