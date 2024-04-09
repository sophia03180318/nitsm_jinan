package com.jcca.web.collect.service;


/**
 * 采集中间业务层
 *
 * @author lyp
 */
public interface CollectLogicService {

    /**
     * 查询采集状态是否正常
     *
     * @param assetId 资产ID
     * @param minute  时长-分钟
     * @return
     * @throws Exception
     */
    Boolean getCollectStatus(String assetId, Integer minute) throws Exception;

}
