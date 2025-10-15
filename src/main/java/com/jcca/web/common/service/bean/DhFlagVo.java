package com.jcca.web.common.service.bean;

import lombok.Data;

import java.util.List;

/**
 * @description: 查询规则配置类
 * @author: sophia
 * @create: 2025/10/13 10:41
 **/
@Data
public class DhFlagVo {
    private String eventId;
    private String statusFlag;
    private List<String> idList;
    private String type;
}