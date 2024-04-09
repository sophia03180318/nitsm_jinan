package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.AssetAttach;

/**
 * @author hanwone
 * @date 2020-04-26 15:35:31
 **/
public interface AssetAttachService extends IService<AssetAttach> {

    AssetAttach getByAssetId(String assetId);
}
