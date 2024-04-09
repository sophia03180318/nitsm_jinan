package com.jcca.common.service;

import java.util.List;

/**
 * @author HanHW
 * @description 基础查询条件
 * @className BaseQueryCondition
 * @date 2023/9/16 14:53
 * @since 2.0.6.0
 */
public class BaseQueryCondition {

    // 与数据库表中字段名一致
    private String colum;
    // 条件值
    private String value;
    private List<Object> valueList;
    // 查询条件
    private BaseQueryEnum condition;

    public String getColum() {
        return colum;
    }

    public void setColum(String colum) {
        this.colum = colum;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Object> getValueList() {
        return valueList;
    }

    public void setValueList(List<Object> valueList) {
        this.valueList = valueList;
    }

    public BaseQueryEnum getCondition() {
        return condition;
    }

    public void setCondition(BaseQueryEnum condition) {
        this.condition = condition;
    }
}
