package com.jcca.web2.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @description: 中间件
 * @author: Lvyp
 * @create: 2023/12/26 10:38
 */
@Data
public class MiddlewareVo {

    public enum Type {
        /**
         * oracle
         */
        ORACLE(1),
        /**
         * mq
         */
        MQ(2);

        private Integer code;

        public Integer getCode() {
            return code;
        }

        Type(Integer code) {
            this.code = code;
        }
    }

    /**
     * Id
     */
    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * IP
     */
    private String ip;
    /**
     * 类型
     */
    private Integer type;

    private String remark;
    /**
     * 创建时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
