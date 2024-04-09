package com.jcca.web.asset.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author syt
 * @date 2021/06/09  15:02
 * @classname nitsmcom.jcca.web.asset.utils.enumsShowTopoEnum
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ShowTopoEnum {
    NOT_SHOW(Byte.valueOf("0"), "不显示"),
    SHOW(Byte.valueOf("1"), "显示");
    private Byte code;
    private String msg;

}
