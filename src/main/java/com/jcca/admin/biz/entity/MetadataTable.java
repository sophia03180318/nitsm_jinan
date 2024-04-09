package com.jcca.admin.biz.entity;

import lombok.Data;

import java.util.List;

/**
 * @author sophia
 * @date 2020-04-23 15:52:15
 **/
@Data
public class MetadataTable {


    /**
     * 表名称
     */
    private String tname;


    /**
     * 字段名称
     */
    private String cname;

    /**
     * 字段集
     */
    private List<String> cnameList;


    /**
     * 字段类型
     */
    private String ctype;

    /**
     * 字段长度
     */
    private String clenght;

}