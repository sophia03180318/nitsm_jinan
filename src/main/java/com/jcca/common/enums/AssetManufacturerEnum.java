package com.jcca.common.enums;

/**
 * 资产厂商
 *
 * @author hanwone
 */

public enum AssetManufacturerEnum {
    HUIPU("惠普", 0),
    IBM("IBM", 1),
    HUAWEI("华为", 2),
    ORACLE("ORACLE", 3),
    CISCO("思科", 4),
    MOXA("MOXA", 5),
    YANHUA("研华", 6),
    LENOVO("联想", 7),
    H3C("H3C", 8),
    MACROSAN("宏杉", 9),
    BJCX("北京从兴", 10),
    CASCO("卡斯柯", 11),
    XDHY("信达环宇", 12),
    TT("图腾", 13),
    AMHY("安盟华御", 14),
    LC("浪潮", 15),
    TH("北京通号", 16),
    BD("宝德", 17),
    DT("东土", 18);


    String name;
    Integer code;

    AssetManufacturerEnum(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static Integer getCode(String name) {
        AssetManufacturerEnum[] values = AssetManufacturerEnum.values();
        for (AssetManufacturerEnum v : values) {
            if (v.getName().equals(name)) {
                return v.getCode();
            }
        }
        return -1;
    }

    public static String getName(Integer code) {
        AssetManufacturerEnum[] values = AssetManufacturerEnum.values();
        for (AssetManufacturerEnum v : values) {
            if (v.getCode().equals(code)) {
                return v.getName();
            }
        }
        return code + "";
    }
}
