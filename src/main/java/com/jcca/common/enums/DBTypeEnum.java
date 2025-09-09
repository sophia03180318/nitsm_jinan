package com.jcca.common.enums;

import lombok.Getter;

/**
 * 数据库类型
 *
 * @author Lvyp
 */
@Getter
public enum DBTypeEnum {

    ORACLE((byte) 1),

    MYSQL((byte) 2),

    DMV8((byte) 4),

    KING((byte) 3);

    private Byte code;

    private DBTypeEnum(Byte code) {
        this.code = code;
    }

    /**
     * 查询code 对应的名称
     *
     * @param code
     * @return
     */
    public static String getNameByCode(Byte code) {
        DBTypeEnum[] values = DBTypeEnum.values();
        for (DBTypeEnum enumItem : values) {
            if (enumItem.getCode().equals(code)) {
                return enumItem.name();
            }
        }

        return code.toString();

    }

}
