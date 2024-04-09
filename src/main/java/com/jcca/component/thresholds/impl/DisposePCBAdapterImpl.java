package com.jcca.component.thresholds.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.jcca.common.utils.EntityBeanUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.component.constants.ReceiveCollectConst;
import com.jcca.component.thresholds.CollectAdapter;
import com.jcca.component.thresholds.bean.CollectPcbBean;
import com.jcca.web.collect.entity.CollectPcb;
import com.jcca.web.collect.service.CollectPcbService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 处理板卡
 *
 * @author Lvyp
 */
@Component
public class DisposePCBAdapterImpl implements CollectAdapter {

    @Resource
    private CollectPcbService pcbService;

    /**
     * 处理板卡采集数据
     *
     * @param data
     */

    @Override
    public void dispose(JSONArray data) {
        List<CollectPcbBean> collectList = JSONUtil.toList(data, CollectPcbBean.class);
        List<CollectPcb> pcbList = new ArrayList<CollectPcb>();
        String collectCode = MyIdUtil.getId();
        for (CollectPcbBean pcb : collectList) {
            if (StrUtil.isEmpty(pcb.getCollectTime()) || StrUtil.isEmpty(pcb.getAssetId())
                    || StrUtil.isEmpty(pcb.getName()) || StrUtil.isEmpty(pcb.getDescStr())
                    || StrUtil.isEmpty(pcb.getModelName()) || StrUtil.isEmpty(pcb.getType())
                    || StrUtil.isEmpty(pcb.getSerialNumber()) || StrUtil.isEmpty(pcb.getSerialNumberName())
                    || StrUtil.isEmpty(pcb.getSoftwareVersion()) || StrUtil.isEmpty(pcb.getHardwareVersion())
                    || StrUtil.isEmpty(pcb.getOsVersion()) || StrUtil.isEmpty(pcb.getPcbIndex())) {
                continue;
            }

            Date date = new Date();
            date.setTime(Long.valueOf(pcb.getCollectTime()));

            CollectPcb pcbData = EntityBeanUtil.copy(pcb, CollectPcb.class);
            pcbData.setCollectTime(date);
            pcbData.setCollectCode(collectCode);
            pcbData.setId(MyIdUtil.getId());
            pcbList.add(pcbData);
        }
        if (pcbList.isEmpty()) {
            return;
        }
        // 缓存板卡信息
        pcbService.updateRealTimeData(pcbList);
        // 更新板卡信息
        pcbService.updateBatchByAssetId(pcbList);
    }

    @Override
    public String getCode() {
        return ReceiveCollectConst.PCB;
    }

}
