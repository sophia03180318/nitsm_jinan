package com.jcca.common.enums;

/**
 * @Description 第三方业务类型
 * @ClassName BusinessTypeEnums
 * @Date 2022/6/6 11:03
 * @Author hanwone
 * @Since 2.0.0.1
 */
public enum BusinessTypeEnums {

    JCCA(0, "中航鼎成"),
    CASCO(1, "卡斯柯"),
    TONGHAO(2, "通号"),
    TIEKE(3, "铁科"),
    BEIYANG(4, "北羊"),
    XDHY(5, "信达环宇"),
    CONGXING(6, "从兴"),
    ;

    public Integer code;
    public String description;

    BusinessTypeEnums(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDesByCode(Integer code) {
        BusinessTypeEnums[] values = BusinessTypeEnums.values();
        for (BusinessTypeEnums value : values) {
            if (value.code.intValue() == code) {
                return value.description;
            }
        }
        return "";
    }
}
