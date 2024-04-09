package com.jcca.web.asset.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author syt
 * @date 2021/06/07  11:19
 * @classname nitsmcom.jcca.web.asset.utils.enumsAssetWatchStatusEnum
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum AssetWatchStatusEnum {

    WATCH_STATUS_NO((byte) 0, "不监控"),
    WATCH_STATUS_YES((byte) 1, "监控"),
    UNKONW((byte) 2, "非法状态");

    private byte code;
    private String sta;

    public static AssetWatchStatusEnum getEnum(Byte code) {
        AssetWatchStatusEnum[] values = AssetWatchStatusEnum.values();
        for (AssetWatchStatusEnum item : values) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return UNKONW;
    }

}
