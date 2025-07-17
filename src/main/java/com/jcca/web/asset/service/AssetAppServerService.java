package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.AssetAppServer;
import com.jcca.web.asset.vo.AssetAppServerVo;

import java.util.List;

/**
 * @author: hhw
 * @description: AssetAppServerService 主要是用来
 * @date: 2025-07-03  16:54
 * @since: 2.0.15.0
 */
public interface AssetAppServerService extends IService<AssetAppServer> {
    List<AssetAppServerVo> getAppServerList();

    List<AssetAppServer> findByAssetIdAndServerPort(String assetId, String serverPort);

    List<AssetAppServer> findByAssetIdNullLink(String assetId);

    List<AssetAppServerVo> getAppServerListByAssetId(String assetId);

    void deleteServerPort(String assetId, Integer serverPort);

    List<AssetAppServer> findByAssetIdAndServerPort2(String assetId, Integer serverPort);

    List<AssetAppServerVo> getAppServerInfo(String assetId);

    void setLinkStatus(String alarmCode, Integer linkStatus);
}
