package com.jcca.web2.vo;

import lombok.Data;

/**
 * 资产指标项状态
 * 固定的CODE ： SERVER_NET_CARD 设备网卡
 * SERVER_PROCESS 设备进程'
 * SERVER_TEMP  设备温度
 *
 * @description: 资产指标项状态
 * @author: Lvyp
 * @create: 2023/12/21 13:52
 */
@Data
public class AssetStatusItmVo {

    public static final Integer NORMAL = 1;
    public static final Integer ERROR = -1;
    public static final String SERVER_NET_CARD = "SERVER_NET_CARD";
    public static final String SERVER_PROCESS = "SERVER_PROCESS";
    public static final String SERVER_TEMP = "SERVER_TEMP";
    public static final String SERVER_FAN = "SERVER_FAN";
    public static final String SERVER_POWER = "SERVER_POWER";
    public static final String SERVER_PORT = "SERVER_PORT";

    /**
     * 代码  此代码可以通过字典与具体的图标联系起来
     */
    private String code;
    /**
     * 代码所对应的标题
     */
    private String title;
    /**
     * 状态
     * 1正常-1异常
     */
    private Integer status;


    /**
     * 是否是基础类型
     *
     * @param code
     * @return
     */
    public static Boolean isBaseItem(String code) {
        return SERVER_NET_CARD.equals(code) || SERVER_PROCESS.equals(code) || SERVER_TEMP.equals(code) || SERVER_FAN.equals(code) || SERVER_POWER.equals(code) || SERVER_PORT.equals(code);
    }

}
