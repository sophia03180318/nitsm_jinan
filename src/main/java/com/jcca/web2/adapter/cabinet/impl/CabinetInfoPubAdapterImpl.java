package com.jcca.web2.adapter.cabinet.impl;

import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.web.collect.entity.CollectCpu;
import com.jcca.web.collect.entity.CollectMemory;
import com.jcca.web.collect.service.CollectCpuService;
import com.jcca.web.collect.service.CollectMemoryService;
import com.jcca.web2.adapter.cabinet.CabinetAssetInfoAdapter;
import com.jcca.web2.vo.CabinetAssetInfoPubVo;
import com.jcca.web2.vo.CabinetAssetInfoVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @description: 设备机柜 详情数据补充 通用数据
 * @author: Lvyp
 * @create: 2023/10/26 11:47
 */
@Service
public class CabinetInfoPubAdapterImpl implements CabinetAssetInfoAdapter {

    @Resource
    private CollectCpuService cpuServ;
    @Resource
    private CollectMemoryService memoryServ;

    @Override
    public List<Integer> getModel() {
        return Arrays.asList(AssetModeConst.SERVER, AssetModeConst.SWITCH, AssetModeConst.ROUTER);
    }

    @Override
    public CabinetAssetInfoVo fattenInfo(CabinetAssetInfoVo baseInfo) {
        List<CollectCpu> cpuArray = cpuServ.getRealTimeData(baseInfo.getId());
        List<CollectMemory> memoryArray = memoryServ.getRealTimeData(baseInfo.getId());

        Date collectTime = null;
        Double cpuUsedRate = null;
        Double memUsedRate = null;
        if (!cpuArray.isEmpty()) {
            collectTime = cpuArray.get(0).getCollectTime();
            cpuUsedRate = cpuArray.get(0).getCpuUsedRate();
        }
        if (!memoryArray.isEmpty()) {
            if (Objects.isNull(collectTime)) {
                collectTime = memoryArray.get(0).getCollectTime();
            }
            memUsedRate = memoryArray.get(0).getMemUsedRate();
        }
        return new CabinetAssetInfoPubVo(baseInfo, collectTime, cpuUsedRate, memUsedRate);
    }
}
