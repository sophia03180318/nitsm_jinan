package com.jcca.web2.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: hhw
 * @description: ItemVo 主要是用来获取用户下组织 资产类型 资产分类列表
 * @date: 2025-05-18  20:16
 * @since: 2.1.6.0
 */
@Data
public class ItemVo {

    private String id;
    private String name;
    private Integer status;

    private String assetDesk;
    private boolean flag;

    private List<ItemVo> children;
}
