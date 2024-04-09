package com.jcca.common.bean;

import java.util.List;

/**
 * @param <T>
 * @author Manager
 */
public class PageBean<T> {

    /**
     * 总记录数
     */
    private Long total;
    /**
     * 分页列表
     */
    private List<T> content;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

}
