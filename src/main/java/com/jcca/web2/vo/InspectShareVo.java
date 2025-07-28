package com.jcca.web2.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: hhw
 * @description: InspectShareVo 主要是用来
 * @date: 2025-07-17  13:46
 * @since: 2.1.8.0
 */
@Data
public class InspectShareVo {

    private String id;
    private String name;
    private Integer autoFlag;
    /**
     * 是否分享, 0没分享，1分享
     */
    private Integer isShare;
    /**
     * 分享人，为空表示自己，不为空表示别人分享的
     */
    private String operator;

    private List<InspectShareVo> children;
}
