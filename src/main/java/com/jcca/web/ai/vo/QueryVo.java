package com.jcca.web.ai.vo;

import lombok.Data;

/**
 * @description: ai查询
 * @author: sophia
 * @create: 2025/04/21 11:37
 **/
@Data
public class QueryVo {
    /**
     * 资产ID
     */
    private String assetId;
    /*
     * 查询天数
     * */
    private Integer day;

    private String processName;
}