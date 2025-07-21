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

    @Select("SELECT * FROM ASSET_APP_SERVER WHERE ASSET_ID = #{assetId} AND SERVER_PORT = #{serverPort} AND LINK_STATUS IS NULL ORDER BY SERVER_PORT")
    List<AssetAppServer> findByAssetIdAndServerPort(String assetId, String serverPort);

    @Select("SELECT * FROM ASSET_APP_SERVER WHERE ASSET_ID = #{assetId} AND LINK_STATUS IS NULL ORDER BY SERVER_PORT")
    List<AssetAppServer> findByAssetIdNullLink(String assetId);

    @Select("select P.*, p.asset_name as itemName from asset_app_server p where p.asset_id = #{assetId} order by p.server_port")
    List<AssetAppServerVo> getAppServerListByAssetId(String assetId);

    @Select("SELECT * FROM ASSET_APP_SERVER WHERE ASSET_ID = #{assetId} AND SERVER_PORT = #{serverPort} AND LINK_STATUS IS NOT NULL ORDER BY SERVER_PORT")
    List<AssetAppServer> findByAssetIdAndServerPort2(String assetId, Integer serverPort);

    List<AssetAppServerVo> getAppServerInfo(@Param("assetId") String assetId);

    @Select("SELECT * FROM ASSET_APP_SERVER WHERE ASSET_ID = #{assetId} AND SERVER_PORT = #{serverPort} AND LINK_IP = #{linkIp} AND LINK_STATUS IS NOT NULL")
    AssetAppServer findOneLinkData(String assetId, Integer serverPort, String linkIp);
}
