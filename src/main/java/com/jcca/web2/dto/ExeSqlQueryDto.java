package com.jcca.web2.dto;

import lombok.Data;

import java.util.List;

/**
 * @description: 请求
 * @author: Lvyp
 * @create: 2023/11/13 14:54
 */
@Data
public class ExeSqlQueryDto {

    private List<ExeSqlQuery> queries;

}
