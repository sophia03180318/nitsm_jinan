package com.jcca.dataProcessing.enums;

import lombok.Getter;

/**
 * 枚举
 *
 * @author zhaozheng
 */
@Getter
public enum EventEnum {


    /**
     * 删除操作
     */
    delete("delete", "删除redisKey下指定mapKey的数据"),
    deleteRedisKey("deleteRedisKey", "删除redisKey相关的所有缓存");


    private String code;
    private String name;

    EventEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getName(String code) {
        EventEnum[] values = EventEnum.values();
        for (EventEnum value : values) {
            if (value.code.equals(code)) {
                return value.name;
            }
        }
        return "UNKNOWN";
    }
}
