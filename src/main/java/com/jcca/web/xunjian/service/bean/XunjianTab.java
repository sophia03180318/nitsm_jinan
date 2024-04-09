package com.jcca.web.xunjian.service.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 巡检结果汇总
 */
@Data
public class XunjianTab {

    private String assetName;
    /**
     * 巡检状态
     */
    private String result;
    /**
     * 设备ID
     */
    private String assetId;
    /**
     * 巡检详情ID
     */
    private String id;

    private String remark;

}
