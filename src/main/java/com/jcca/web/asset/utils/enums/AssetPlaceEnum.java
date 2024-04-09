package com.jcca.web.asset.utils.enums;

import lombok.Getter;

/**
 * 资产方位
 *
 * @author Lvyp
 */
@Getter
public enum AssetPlaceEnum {

    CENTRE(2, "中心"),

    STATION(4, "车站"),

    UNKONW(-1, "");

    private Integer type;
    private String msg;

    private AssetPlaceEnum(Integer type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public static AssetPlaceEnum getEnum(Integer type) {
        AssetPlaceEnum[] values = AssetPlaceEnum.values();
        for (AssetPlaceEnum item : values) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return UNKONW;
    }

}
