package com.jcca.web.asset.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author syt
 * @date 2021/06/07  11:43
 * @classname nitsmcom.jcca.web.asset.utils.enumsAssetStatusEnum
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AssetStatusEnum {

    ASSET_STATUS_OFFLINE(Byte.valueOf("0"), "离线"),
    ASSET_STATUS_ONLINE(Byte.valueOf("1"), "在线"),
    ASSET_STATUS_NO_WATCH(Byte.valueOf("2"), "不监控"),
    UNKONW(Byte.valueOf("4"), "未知");

    private Byte code;
    private String sta;

    public static AssetStatusEnum getEnum(Byte code) {
        AssetStatusEnum[] values = AssetStatusEnum.values();
        for (AssetStatusEnum item : values) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return UNKONW;
    }

}
