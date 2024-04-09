package com.jcca.web2.service.notify;

import com.jcca.web.asset.entity.Asset;
import com.jcca.web.common.constants.OutConst;
import com.jcca.web.common.service.OutService;
import com.jcca.web2.service.AssetNotifyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 资产变动通知采集器
 * @className NotifyCollectorImpl
 * @date 2023/12/5 16:38
 * @since 2.1.0.0
 */
@Service
public class NotifyCollectorImpl implements AssetNotifyService {

    @Resource
    private OutService outService;

    /**
     * OutConst
     * 0新增，1删除，2修改
     *
     * @param asset 变动的资产
     * @param state 0新增，1删除，2修改
     */
    @Override
    public void assetChange(Asset asset, Integer state) {
        if (OutConst.ALL_ASSET_UPDATE.intValue() == state) {
            return;
        }
        outService.notifyOnChange(state, asset);
    }
}
