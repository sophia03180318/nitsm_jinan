package com.jcca.web.xunjian.controller.bean;

import com.jcca.web.asset.entity.Asset;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 发起巡检请求Bean
 * V2
 */
@Data
public class BeginXunJianReq implements Serializable {

    /**
     * 资产列表
     */
    @NotEmpty(message = "巡检资产列表不能空")
    private List<String> assetIdList;
    /**
     * 巡检指标
     */
    private List<String> targetList;

}
