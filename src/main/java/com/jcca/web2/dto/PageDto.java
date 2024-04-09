package com.jcca.web2.dto;

import lombok.Data;

/**
 * @description: 分页参数
 * @author: Lvyp
 * @create: 2023/11/10 18:11
 */
@Data
public class PageDto {
    /**
     * 页
     */
    private Integer page;
    /**
     * 页大小
     */
    private Integer size;
    /**
     * 排序的字段
     */
    private String orderBy;
    /**
     * 排序方式
     * asc,desc
     */
    private String sort;

}
