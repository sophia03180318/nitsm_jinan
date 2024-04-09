package com.jcca.web2.service;

import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.bean.AddAssetException;

/**
 * @author HanHW
 * @description 资产变动通知外部应用
 * @className AssetNotifyService
 * @date 2023/12/5 16:35
 * @since 2.1.0.0
 */
public interface AssetNotifyService {

    /**
     * OutConst
     * 0新增，1删除，2修改监控字段，3修改所有字段
     *
     * @param asset 变动的资产
     * @param state 0新增，1删除，2修改监控字段，3修改所有字段
     */
    void assetChange(Asset asset, Integer state) throws AddAssetException;
}
