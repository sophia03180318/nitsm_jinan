package com.jcca.web2.service.notify;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.service.AssetNotifyService;
import com.jcca.web2.service.InspectRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author HanHW
 * @description 资产变动通知巡检
 * @className NotifyCollectorImpl
 * @date 2024/01/22 09:38
 * @since 2.1.0.0
 */
@Service
public class NotifyInspectImpl implements AssetNotifyService {

    @Resource
    private InspectRecordService inspectRecordService;

    /**
     * OutConst
     * 0新增，1删除，2修改
     *
     * @param asset 变动的资产
     * @param state 0新增，1删除，2修改
     */
    @Override
    public void assetChange(Asset asset, Integer state) {
        UpdateWrapper<InspectRecord> update = Wrappers.update();
        update.eq("asset_id", asset.getId());
        inspectRecordService.remove(update);

        inspectRecordService.checkRecord();
    }
}
