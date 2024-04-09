package com.jcca.dataProcessing.DataFilter.raid;

import com.jcca.dataProcessing.Entity.DiskEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectRaid;
import com.jcca.web.collect.service.CollectRaidService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author Zhaozheng
 * @description TODO Raid存储 容量信息过滤处理类
 * @className StorageCapacityFilterHandler
 * @date 2023/10/27 9:35
 * @since 2.1.0.0
 */
@Component("raidVSaveFilterHandler")
public class RaidVSaveFilterHandler extends IFilterHandler<DiskEntity> {

    @Resource
    private CollectRaidService raidService;

    @Override
    public boolean handler(DiskEntity info) {

        CollectRaid collectRaid = new CollectRaid();
        BeanUtils.copyProperties(info, collectRaid);
        collectRaid.setAssetId(info.getAssetId());
        if (info.getCapacity() != null) {
            collectRaid.setCapacity(Long.parseLong(info.getCapacity()));
        }
        if (info.getUsedCapacity() != null) {
            collectRaid.setUsedCapacity(Long.parseLong(info.getUsedCapacity()));
        }
        if (info.getCollectTime() == null) {
            collectRaid.setCollectTime(new Date());
        } else {
            collectRaid.setCollectTime(new Date(info.getCollectTime()));
        }

        collectRaid.setCollectCode(info.getCollectCode());
        raidService.saveOrUpdate(collectRaid);
        return true;
    }

    @Override
    public boolean isNeedNexthandle(Boolean flag) {
        return flag;
    }


}
