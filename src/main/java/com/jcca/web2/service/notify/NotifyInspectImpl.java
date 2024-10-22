package com.jcca.web2.service.notify;

import com.jcca.web.asset.entity.Asset;
import com.jcca.web.common.constants.OutConst;
import com.jcca.web2.service.AssetNotifyService;
import com.jcca.web2.service.InspectRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (OutConst.MODIFY_ASSET.intValue() == state) {
            this.modifyInspect(asset);
        }

        if (OutConst.ADD_ASSET.intValue() == state) {
            this.addInspect();
        }

    }

    private void addInspect() {
        inspectRecordService.checkRecord();
    }

    private void modifyInspect(Asset asset) {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        map.put("assetId", asset.getId());
        map.put("status", asset.getWatch() + "");
        list.add(map);
        inspectRecordService.modifyAsset(list);
    }
}
