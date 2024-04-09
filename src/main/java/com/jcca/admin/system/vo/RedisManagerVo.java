package com.jcca.admin.system.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ Author：sophia
 * @ Date：Created in 10:06 2022/5/10
 * @ Description:
 */
@Data
public class RedisManagerVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * key
     */
    @NotNull(message = "key不可为空")
    private String key;
    /**
     * value
     */
    @NotNull(message = "value不可为空")
    private String value;
    /**
     * 时间
     */

    private String expireTime;

    /**
     * 数据格式
     */

    private int type;


}
