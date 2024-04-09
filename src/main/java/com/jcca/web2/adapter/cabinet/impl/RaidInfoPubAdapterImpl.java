package com.jcca.web2.adapter.cabinet.impl;

import cn.hutool.core.util.ObjectUtil;
import com.jcca.common.bean.constant.AssetModeConst;
import com.jcca.web.collect.entity.CollectDS;
import com.jcca.web.collect.entity.CollectRaid;
import com.jcca.web.collect.service.CollectDsService;
import com.jcca.web.collect.service.CollectRaidService;
import com.jcca.web2.adapter.cabinet.CabinetAssetInfoAdapter;
import com.jcca.web2.vo.CabinetAssetInfoRaidVo;
import com.jcca.web2.vo.CabinetAssetInfoVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @description: 磁盘阵列相关详情补充
 * @author: Lvyp
 * @create: 2023/10/30 09:37
 */
@Service
public class RaidInfoPubAdapterImpl implements CabinetAssetInfoAdapter {

    private static final String RAID_V = "V";

    @Resource
    private CollectRaidService raidService;
    @Resource
    private CollectDsService dsService;


    @Override
    public List<Integer> getModel() {
        return Arrays.asList(AssetModeConst.RAID);
    }

    @Override
    public CabinetAssetInfoVo fattenInfo(CabinetAssetInfoVo baseInfo) {
        String assetImage = baseInfo.getAssetImage();
        if (assetImage.toUpperCase().startsWith(RAID_V)) {
            List<CollectRaid> capacityList = raidService.findByType(baseInfo.getId(), null, 3);
            if (ObjectUtil.isNotNull(capacityList) && !capacityList.isEmpty()) {
                CollectRaid capacity = capacityList.get(0);
                return new CabinetAssetInfoRaidVo(baseInfo, capacity.getCollectTime(), capacity.getCapacity(), capacity.getUsedCapacity());
            }
        } else {
            List<CollectDS> capacityList = dsService.findByType(baseInfo.getId(), 4);
            if (Objects.nonNull(capacityList) && !capacityList.isEmpty()) {
                CollectDS capacity = capacityList.get(0);
                long usedCapacity = capacity.getCapacity() - capacity.getFreeCapacity();
                return new CabinetAssetInfoRaidVo(baseInfo, capacity.getCollectTime(), capacity.getCapacity(), usedCapacity);
            }

        }

        return baseInfo;
    }
}
