package com.jcca.web.asset.utils.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * U位使用情况
 *
 * @author 苏一拓
 * @date 2021/5/12/012  17:19
 * @classname nitsmcom.jcca.web.asset.utils.beanCabinetUsed
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CabinetUsed {

    // U位
    private Integer positon;

    // false未使用, true已使用
    private boolean usedState;
}
