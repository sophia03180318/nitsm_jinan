package com.jcca.web.asset.utils.enums;

import lombok.Getter;

/**
 * 产品类型
 *
 * @author Lvyp
 */
@Getter
public enum ProductTypeEnum {

    LYQ(42, "路由器"),
    JHJ(201, "交换机"),
    AQBJ(300, "安全边界"),
    FHQ(400, "防火墙"),
    WZ(500, "网闸"),
    XZ(600, "协转");

    private Integer code;
    private String msg;


    public static ProductTypeEnum getEnum(Integer code) {
        ProductTypeEnum[] values = ProductTypeEnum.values();
        for (ProductTypeEnum manufacturersEnum : values) {
            if (manufacturersEnum.getCode().equals(code)) {
                return manufacturersEnum;
            }
        }

        return null;
    }

    public static String changeProduct(ProductTypeEnum productEnum) {
        StringBuilder str = new StringBuilder("");
        ProductTypeEnum[] values = ProductTypeEnum.values();
        for (ProductTypeEnum product : values) {
            str.append(product.getMsg());
            if (product == productEnum) {
                str.append("■");
            } else {
                str.append("□");
            }
            str.append("   ");
        }

        return str.toString();
    }

    private ProductTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }


}
