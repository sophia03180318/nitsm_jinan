package com.jcca.web.collect.enums;

import lombok.Getter;

/**
 * 采集数据库的类型
 *
 * @author Lvyp
 */
@Getter
public enum CollectDBTypeEnum {

    ORACLE((byte) 1),
    MYSQL((byte) 2);

    private Byte code;

    private CollectDBTypeEnum(Byte code) {
        this.code = code;
    }

}
