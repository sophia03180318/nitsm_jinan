package com.jcca.web2.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: hhw
 * @description: ItemVo 主要是做为通用模型使用
 * @date: 2025-05-18  20:16
 * @since: 2.1.6.0
 */
@Data
public class ItemVo {

    private String id;
    private String name;

    private Integer status;
    private Integer total;
    private Integer normal;
    private Integer abnormal;
    private Integer autoFlag;

    private String assetDesk;
    private String eventCategory;
    private boolean flag;
    /**
     * add by lfp
     * 用于智能巡检模板 适配组织机构父子关系
     */
    private String orgId;

    private List<ItemVo> children;
}
