package com.jcca.web.auth.vo;

import lombok.Data;

/**
 * @ClassName SysDictVo
 * @Description TODO
 * @Date 2020/4/23 15:55
 * @Author hanwone
 */
@Data
public class SysDictVo {
    private String key;
    private String value;

    private String pValue;
    private String pKey;
}
