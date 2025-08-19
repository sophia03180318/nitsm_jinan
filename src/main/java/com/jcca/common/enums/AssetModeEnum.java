package com.jcca.common.enums;

import com.jcca.common.bean.constant.AssetModeConst;

/**
 * 资产种类
 *
 * @author hanwone
 */

public enum AssetModeEnum {
    FIRE_WALL("防火墙", AssetModeConst.FIRE_WALL), ROUTER("路由器", AssetModeConst.ROUTER),
    STORE("存储设备", AssetModeConst.STORE), SMALL_SERVER("小型机", AssetModeConst.SMALL_SERVER),
    SERVER("服务器", AssetModeConst.SERVER), SWITCH("交换机", AssetModeConst.SWITCH),

    TERMINAL("终端", AssetModeConst.TERMINAL), IPC("工控机", AssetModeConst.IPC),
    DB("数据库", AssetModeConst.DB), RAID("磁盘阵列", AssetModeConst.RAID);

    String name;

    int code;

    AssetModeEnum(String name, int code) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

}
