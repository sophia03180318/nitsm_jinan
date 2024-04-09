package com.jcca.web2.enums;

import com.jcca.common.bean.constant.AssetModeConst;
import lombok.Getter;

/**
 * @author HanHW
 * @description 采集类型, 通过 Asset表assetMode和collectionType组合而来
 * @className ModeTypeEnum
 * @date 2023/11/16 16:03
 * @since 2.1.0.0
 */
@Getter
public enum ModeTypeEnum {

    // type取值： 0-linux设备，1-windows设备，2-aix设备，-1-网络设备
    LINUX(AssetModeConst.SERVER, 0, "LINUX设备"),
    WINDOWS(AssetModeConst.SERVER, 1, "WINDOWS设备"),
    AIX(AssetModeConst.SERVER, 2, "AIX设备"),
    RAID(AssetModeConst.RAID, 0, "存储设备"),
    SWITCH(AssetModeConst.SWITCH, -1, "交换机设备"),
    ROUTER(AssetModeConst.ROUTER, -1, "路由器设备"),

    ;

    Integer mode;
    Integer type;
    String name;

    ModeTypeEnum(Integer mode, Integer type, String name) {
        this.mode = mode;
        this.type = type;
        this.name = name;
    }

    public static String getNameStr(int mode, int type) {
        ModeTypeEnum[] values = ModeTypeEnum.values();
        for (ModeTypeEnum value : values) {
            if (value.mode == mode && value.type == type) {
                return value.name;
            }
        }

        return "其它";
    }

}
