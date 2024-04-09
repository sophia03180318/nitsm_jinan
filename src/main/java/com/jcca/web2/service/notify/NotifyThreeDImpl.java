package com.jcca.web2.service.notify;

import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.bean.AddAssetException;
import com.jcca.web.common.service.ThreeDService;
import com.jcca.web2.service.AssetNotifyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description: 资产变动通知3D机房
 * @author: sophia
 * @create: 2023/12/27 11:36
 **/
@Service
public class NotifyThreeDImpl implements AssetNotifyService {
    @Resource
    ThreeDService threeDService;

    /**
     * state ：0新增，1删除，2修改
     */
    @Override
    public void assetChange(Asset asset, Integer state) throws AddAssetException {
        if (state == 0) {
            threeDService.pushAssetAdd(asset);
        } else if (state == 1) {
            threeDService.pushAssetRemove(asset.getId());
        } else if (state == 2) {
            threeDService.pushAssetChange(asset);
        }
    }
}