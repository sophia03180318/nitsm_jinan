package com.jcca.web.asset.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.AssetAppServer;
import com.jcca.web.asset.vo.AssetAppServerVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: hhw
 * @description: AssetAppServerMapper 主要是用来
 * @date: 2025-07-03  16:51
 * @since: 2.0.15.0
 */
public interface AssetAppServerMapper extends BaseMapper<AssetAppServer> {

    List<AssetAppServerVo> getAppServerList();

    List<AssetAppServer> findByAssetIdAndServerPort(String assetId, String serverPort);

    List<AssetAppServer> findByAssetIdNullLink(String assetId);

    List<AssetAppServerVo> getAppServerListByAssetId(String assetId);

    List<AssetAppServer> findByAssetIdAndServerPort2(String assetId, Integer serverPort);

    List<AssetAppServerVo> getAppServerInfo(@Param("assetId") String assetId);

    AssetAppServer findOneLinkData(String assetId, Integer serverPort, String linkIp);
}
