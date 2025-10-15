package com.jcca.web.common.service.bean;

import lombok.Data;

/**
 * @description: 动环组织树节点
 * @author: sophia
 * @create: 2025/10/13 13:51
 **/
@Data
public class DhNodeVo {
    private String id;
    private String parentId;
    private String name;
    private String type;
}