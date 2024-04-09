package com.jcca.dataProcessing.Entity;

import lombok.Data;

import java.util.Date;

@Data
public class CustomEvent extends CommonEntity {

    /**
     * 匹配码
     */
    private String uniqueCode;
    /**
     * 类型
     */
    private String type;
    /**
     * 日志信息
     */
    private String msg;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 唯一标识
     * */
    private String flag;

    /**
     * 状态  1=恢复  -1=异常
     * */
    private Integer status;
}
