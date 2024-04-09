package com.jcca.web.asset.service.bean;

import com.jcca.web.asset.enums.ThresholdSectionEnum;
import lombok.Data;

/**
 * 校验阈值请求
 *
 * @author lyp
 */
@Data
public class VerifyThresholdReq {

    /**
     * 采集值
     */
    private String collectValue;
    /**
     * 资产ID
     */
    private String assetId;
    /**
     * 标记 一般是端口名称
     */
    private String flag;
    /**
     * 阈值类型
     */
    private ThresholdSectionEnum type;
    /**
     * 是否需要校验区间阈值
     * 目前只有折线图会有区间阈值
     */
    private Boolean haveSection;
    /**
     * 实时流入或者流出速率
     */
    private Double portIntOrOutSpeed;


}
