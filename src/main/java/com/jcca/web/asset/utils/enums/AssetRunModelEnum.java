package com.jcca.web.asset.utils.enums;

import lombok.Getter;

/**
 * 资产运行方式
 *
 * @author Lvyp
 */
@Getter
public enum AssetRunModelEnum {

    ONE("单机部署"),

    MORE("双机热备");

    private String msg;

    private AssetRunModelEnum(String msg) {
        this.msg = msg;
    }


    public static String getMsg(String name) {
        AssetRunModelEnum[] values = AssetRunModelEnum.values();
        for (AssetRunModelEnum assetModel : values) {
            if (assetModel.name().equals(name)) {
                return assetModel.getMsg();
            }
        }
        return "";
    }


}
