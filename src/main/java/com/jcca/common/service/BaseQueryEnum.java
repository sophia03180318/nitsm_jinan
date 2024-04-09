package com.jcca.common.service;

/**
 * @author HanHW
 * @description 简单查询条件。复杂查询条件需要手动拼装
 * @className BaseQueryEnum
 * @date 2023/9/16 14:54
 * @since 2.0.6.0
 */
public enum BaseQueryEnum {

    //    EQ(1, "等于"),
    LE(2, "小于"),
    LT(3, "小于等于"),
    GT(4, "大于"),
    GE(5, "大于等于"),
    NE(6, "不等于"),
    LIKE(7, "%包含%"),
    //    LIKE_LEFT(8, "%以什么结尾"),
    //    LIKE_RIGHT(9, "以什么开头%"),
    IN(10, "在范围内"),
    //    OR(11, "或者"),
    //    BETWEEN(12, "在区间内"),
    ORDER_ASC(13, "正序排列"),
    ORDER_DESC(14, "倒序排列"),
    ;

    private Integer type;
    private String content;

    BaseQueryEnum(Integer type, String content) {
        this.type = type;
        this.content = content;
    }

    public Integer getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}
