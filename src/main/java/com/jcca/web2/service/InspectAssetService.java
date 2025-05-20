package com.jcca.web2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web2.entity.InspectAsset;

import java.util.List;

/**
 * @author HanHW
 * @description 巡检管理服务
 * @className InspectAssetService
 * @date 2025/5/19 17:31
 * @since 2.1.6.0
 */
public interface InspectAssetService extends IService<InspectAsset> {

    List<InspectAsset> getInspectAssets(List<String> assetIds);
}
