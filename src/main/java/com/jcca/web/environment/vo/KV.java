package com.jcca.web.environment.vo;

import lombok.Data;

/**
 * @ClassName KV
 * @Description TODO
 * @Date 2020/5/18 18:18
 * @Author hanwone
 */
@Data
public class KV {
    private String key;
    private String value;

    public KV() {
    }

    ;

    public KV(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
