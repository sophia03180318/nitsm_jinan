package com.jcca.web.collect.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 硬件端口状态
 *
 * @author Lvyp
 */
@Getter
public enum InterfaceStatus {
    //采集器采集mib号 回来为空 会替换为0
    UNKINOW((byte) 0, "未知"),

    OK((byte) 1, "启用"),

    NO((byte) 2, "未启用"),
    //忽略
    TESTING((byte) 3, "测试状态"),
    //忽略
    UNKINOW2((byte) 4, "未知"),
    //物理上通,等待外部事件
    DORMANT((byte) 5, "休眠状态"),
    //缺组件 或者逻辑上被关闭
    NOTPRESENT((byte) 6, "不存在"),
    //下层端口全部非up
    LOWERLAYERDOWN((byte) 7, "下层状态down");

    private Byte code;
    private String msg;

    private InterfaceStatus(Byte code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static Boolean isUp(Byte code) {
        List<Byte> asList = Arrays.asList(OK.getCode(), DORMANT.getCode());
        if (asList.contains(code)) {
            return true;
        }
        return false;
    }


    /**
     * 需要忽略的交换机状态
     * true 此状态不可做告警判定 需要忽略掉
     * false 此状态可以做告警判定
     * @return
     */
    public static Boolean needIgnore(Byte code){
        List<Integer> knownStatusList = Arrays.asList(1, 5, 2, 6, 7);
        return !knownStatusList.contains(code.intValue());
    }

}
