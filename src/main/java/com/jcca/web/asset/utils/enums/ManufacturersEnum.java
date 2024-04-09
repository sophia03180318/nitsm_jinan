package com.jcca.web.asset.utils.enums;

import lombok.Getter;

/**
 * 设备厂家
 *
 * @author Lvyp
 */
@Getter
public enum ManufacturersEnum {

    JCCA("中航嘉城"),
    CASCO("卡斯柯"),
    TK("铁科"),
    TH("通号"),
    BY("北羊"),
    BJCX("北京从兴"),
    AMS("艾默生");

    private String msg;


    public static ManufacturersEnum getEnum(String code) {
        ManufacturersEnum[] values = ManufacturersEnum.values();
        for (ManufacturersEnum manufacturersEnum : values) {
            if (manufacturersEnum.name().equals(code)) {
                return manufacturersEnum;
            }
        }

        return null;
    }

    public static String changeManufacturers(ManufacturersEnum manufacturers) {
        StringBuilder str = new StringBuilder("");
        ManufacturersEnum[] values = ManufacturersEnum.values();
        for (ManufacturersEnum manufacturersEnum : values) {
            str.append(manufacturersEnum.getMsg());
            if (manufacturersEnum == manufacturers) {
                str.append("■");
            } else {
                str.append("□");
            }
            str.append("   ");
        }

        return str.toString();
    }

    private ManufacturersEnum(String msg) {
        this.msg = msg;
    }


}
