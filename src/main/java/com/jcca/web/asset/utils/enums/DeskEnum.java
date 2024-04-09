package com.jcca.web.asset.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author syt
 * @date 2021/06/09  14:57
 * @classname nitsmcom.jcca.web.asset.utils.enumsDeskEnum
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum DeskEnum {
    TERMINAL(1, "终端"),
    SMALL_SERVER(2, "小型机"),
    IPC(3, "工控机");
    private Integer code;
    private String desk;
}
