package com.jcca.web2.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * group 的作用实际上是查询出一个唯一条件
 * 作为sql的筛选条件拼接到sql中去
 *
 * @description: 执行查询sql的请求参数
 * @author: Lvyp
 * @create: 2023/11/01 16:53
 */
@Data
public class ExeSqlQuery {

    /**
     * db数据源
     */
    @NotEmpty(message = "缺少数据源")
    private String dbSource;
    /**
     * 查询名称
     * 如果groupSql是空会放入响应的name中
     */
    private String queryName;
    /**
     * 分组标识
     * ORACLE数据源必须 as key
     * 如果有分组标识将先执行分组查询在将分组标识转为筛选条件
     */
    private String groupSql;
    /**
     * 分组标识
     */
    private String groupField;
    /**
     * 需要执行的单条sql
     * ORACLE 到where 1=1
     * ORACLE 必须as key as value  key 为时间  value为值
     */
    @NotEmpty(message = "缺少执行语句")
    private String sql;

    /**
     * 开始时间时间戳 秒
     */
    @NotNull(message = "缺少开始时间")
    private Long start;
    /**
     * 结束时间时间戳 秒
     */
    @NotNull(message = "缺少结束时间")
    private Long end;
    /**
     * 时间筛选字段
     */
    @NotEmpty(message = "缺少时间字段")
    private String timeField;

    /**
     * 过滤的字段
     */
    private String searchName;
    /**
     * 过滤的值
     */
    private String searchValue;

}
