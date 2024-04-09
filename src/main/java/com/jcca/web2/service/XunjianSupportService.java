package com.jcca.web2.service;

import com.jcca.web2.vo.InspectResultVo;
import com.jcca.web2.vo.InspectVo;

import java.util.Date;
import java.util.List;

/**
 * @author Zhaozheng
 * @description TODO 巡检数据支持服务
 * @className XunjianSupportService
 * @date 2024/1/11 17:03
 * @since 2.1.0.0
 */
public interface XunjianSupportService {
    /**
     * 巡检指标
     *
     * @return
     */
    public List<InspectVo> inspectItem();

    /**
     * 资产巡检
     *
     * @return
     */
    public List<InspectResultVo> inspect(String assetId, String assetIp, List<String> targetList);

    public List<InspectResultVo> remoteInspect(String assetId, List<String> inspectItems, Date creatDate);

}
