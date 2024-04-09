package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.AssetHidConf;

import java.util.List;


/**
 * 资产隐藏配置
 */
public interface AssetHidConfService extends IService<AssetHidConf> {

    /**
     * 查询隐藏的列表
     * @param assetId
     * @param name
     * @return
     */
    List<AssetHidConf> getFlagListByAsset(String assetId, String name);

    /**
     * 更新设备的隐藏配置
     * @param assetId
     * @param saveList
     */
    void updateAssetNetCardConf(String assetId, List<AssetHidConf> saveList);


    /**
     * 更新设备的隐藏配置
     * @param assetId
     * @param saveList
     */
    void updateAssetPortConf(String assetId, List<AssetHidConf> saveList);
}
