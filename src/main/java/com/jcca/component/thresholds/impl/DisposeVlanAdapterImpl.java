package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectVlanBean;
import com.jcca.web.collect.entity.CollectVlan;
import com.jcca.web.collect.service.CollectVlanService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * vlan 数据处理
 *
 * @author Lvyp
 */
@Component
public class DisposeVlanAdapterImpl implements CollectAdapter {

    @Resource
    private CollectVlanService vlanService;

    /**
     * 处理vlan数据
     *
     * @param data
     */

    @Override
    public void dispose(JSONArray data) {
        List<CollectVlanBean> vlanList = JSONUtil.toList(data, CollectVlanBean.class);
        String collectCode = MyIdUtil.getId();
        List<CollectVlan> vlans = new ArrayList<CollectVlan>();
        for (CollectVlanBean item : vlanList) {
            if (StrUtil.isEmpty(item.getAssetId()) || StrUtil.isEmpty(item.getCollectTime())
                    || StrUtil.isEmpty(item.getVlanIndex()) || StrUtil.isEmpty(item.getVlanName())
                    || StrUtil.isEmpty(item.getVlanStatus()) || StrUtil.isEmpty(item.getVlanType())) {
                continue;
            }
            Date collectTime = new Date();
            collectTime.setTime(Long.valueOf(item.getCollectTime()));

            CollectVlan vlan = EntityBeanUtil.copy(item, CollectVlan.class);
            vlan.setCollectCode(collectCode);
            vlan.setCollectTime(collectTime);
            vlan.setId(MyIdUtil.getId());
            vlans.add(vlan);
        }

        if (vlans.isEmpty()) {
            return;
        }
        // 刷新实时数据
        vlanService.updateRealTimeData(vlans);
        // 更新
        vlanService.updateBatchByAssetId(vlans);

    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.VLAN;
    }

}
