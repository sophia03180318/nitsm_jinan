package com.jcca.web.common.service.bean;

import lombok.Data;
import org.apache.ibatis.annotations.Delete;

/**
 * @description: 3D机房
 * @author: sophia
 * @create: 2024/09/06 09:32
 **/
@Data
public class ThreeMap {
    private String key;
    private String value;

    public ThreeMap(String key, String value) {
        this.key = key;
        this.value = value;
    }
}