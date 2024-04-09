package com.jcca.web.asset.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author syt
 * @date 2021/08/24  14:39
 * @classname nitsmcom.jcca.web.asset.utils.enumsProcessHostModeEnum
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ProcessHostModeEnum {
    DOUBLE_HOST_DOUBLE_LIVE(2, "双机双活"),
    DOUBLE_HOST_SINGLE_LIVE(1, "双机单活"),
    COMMON(3, "普通");
    private Integer code;
    private String name;
}
