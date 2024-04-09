package com.jcca.web.xunjian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.xunjian.entity.XunjianAsset;

/**
 * @author hanwone
 * @date 2021-02-24 16:09:21
 **/
public interface XunjianAssetService extends IService<XunjianAsset> {

    /**
     * 删除
     *
     * @param assetId
     */
    void removeByAssetId(String assetId);

}
