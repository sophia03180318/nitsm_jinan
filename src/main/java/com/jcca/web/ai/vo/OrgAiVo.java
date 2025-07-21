package com.jcca.web.ai.vo;

import lombok.Data;

/**
 * @description: 组织请求
 * @author: sophia
 * @create: 2025/04/10 10:24
 **/
@Data
public class OrgAiVo {

    /**
     * 组织ID
     */
    private String id;
    /**
     * 组织名称
     */
    private String title;

    private Integer status;

    private String type;
    private String pid;
}