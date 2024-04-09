package com.jcca.component.casco.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务器主备-运行状态
 *
 * @author Lvyp
 */
@AllArgsConstructor
@Getter
public enum HostRunStatusEnum {

    STOP("中断"),

    MASTER("主机"),

    DEFNULL("未知"),

    BACK("备机");

    private String name;

    public static String getName(String cont) {
        HostRunStatusEnum[] values = HostRunStatusEnum.values();
        for (HostRunStatusEnum item : values) {
            if (item.name().equals(cont)) {
                return item.getName();
            }
        }
        return cont + "";
    }

}
