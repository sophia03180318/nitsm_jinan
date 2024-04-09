package com.jcca.common.enums;

import lombok.Getter;

/**
 * @ClassName AssetHardwareTypeEnum
 * @Description 资产硬件类型
 * @Date 2020/7/16 19:32
 * @Author hanwone
 */
@Getter
public enum AssetHardwareTypeEnum {
    CPU(1, "CPU"),
    MEMORY(2, "内存"),
    DISK(3, "硬盘"),
    POWER(4, "电源"),
    PCB(5, "板卡"),
    FAN(6, "风扇"),
    ENGINE(7, "引擎"),
    CPU_CARD(8, "CPU板"),
    IO_CARD(9, "IO板"),
    CONTROLLER(10, "控制器"),
    VOLTAGE_MODULE(11, "稳压模块");

    private Integer code;

    private String decription;

    public static String getDecrip(Integer code) {
        AssetHardwareTypeEnum[] values = AssetHardwareTypeEnum.values();
        for (AssetHardwareTypeEnum item : values) {
            if (item.getCode().equals(code)) {
                return item.getDecription();
            }
        }
        return "";
    }

    AssetHardwareTypeEnum(Integer code, String decription) {
        this.code = code;
        this.decription = decription;
    }

}
