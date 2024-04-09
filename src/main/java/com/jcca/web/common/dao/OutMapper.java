package com.jcca.web.common.dao;

import com.jcca.web.asset.vo.AssetOutVo;
import com.jcca.web.common.controller.req.AssetOutReq;

import java.util.List;

/**
 * @ClassName OutMapper
 * @Description ITSM调用外部应用接口
 * @Date 2020/7/31 11:42
 * @Author hanwone
 */
public interface OutMapper {

    List<AssetOutVo> findForOutRequest(AssetOutReq req);

    List<AssetOutVo> findStationAssetByGroup(AssetOutReq req);

    /**
     * 查询是否存在记录主备状态的表
     *
     * @return
     */
    Integer findMasterOrSlaveTab(String tabName);

    /**
     * 查询主备状态
     */
    List<String> findMasterOrSlave(String assetId);

}
