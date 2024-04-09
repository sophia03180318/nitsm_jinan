package com.jcca.web.topo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.common.service.bean.BusinessGetSnmpResultReq;
import com.jcca.web.topo.entity.NetworkAssetMap;

/**
 * 拓扑发现
 *
 * @author lyp
 */
public interface NetworkAssetMapService extends IService<NetworkAssetMap> {

    /**
     * 发起拓扑发现
     *
     * @param req
     * @throws Exception
     */
    void beginSeach(BusinessGetSnmpResultReq req) throws Exception;

    /**
     * 查询所有
     *
     * @throws Exception
     */
    void beginSeachAll() throws Exception;

    /**
     * 查询资产IP
     *
     * @param remoteIp
     * @return
     */
    String findAssetIpByRemoteIp(String remoteIp);

    /**
     * 验证TOPO发现是否完成
     * true 完成，false 未完成
     *
     * @param assetId
     */
    Boolean verifyTopoFindStatus(String assetId);

}
