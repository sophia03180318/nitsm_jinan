package com.jcca.web2.vo;

import lombok.Data;

/**
 * @description: 端口配置
 * @author: Lvyp
 * @create: 2024/01/10 11:18
 */
@Data
public class InterfacesConfigVo {

    /**
     * 模板中的名称
     */
    private String tempPortName;
    /**
     * 端口名称
     */
    private String portName;
    /**
     * 全名
     */
    private String portIndex;
}
