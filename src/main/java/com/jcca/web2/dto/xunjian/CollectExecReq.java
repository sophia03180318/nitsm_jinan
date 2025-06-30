package com.jcca.web2.dto.xunjian;

import lombok.Data;

import java.util.List;

/**
 * @author: hhw
 * @description: CollectExecReq 主要是用来
 * @date: 2025-05-27  15:45
 * @since: 2.1.6.0
 */
@Data
public class CollectExecReq {
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 巡检任务ID
     */
    private String inspectRecordId;
    /**
     * 巡检类型
     * 空 全部巡检
     * 非空 只采集传值的类型
     */
    private List<String> categoryList;
}
