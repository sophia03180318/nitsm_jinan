package com.jcca.web.asset.controller.bean;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName AssetHistoryReq
 * @Description 资产历史性能信息查询
 * @Date 2020/7/6 16:04
 * @Author hanwone
 */
@Data
public class AssetHistoryReq {
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 查询周期，0今天，3近三天，7近一周，30近三十天，-1自定义时间
     */
    private int period;
    /**
     * 自定义开始时间
     */
    private Date startDate;
    /**
     * 自定义结束时间
     */
    private Date endDate;
}
