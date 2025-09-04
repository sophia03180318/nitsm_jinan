package com.jcca.dataProcessing.DataFilter.raid;

import com.jcca.common.utils.MyIdUtil;
import com.jcca.dataProcessing.Entity.RaidCommonLogEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.collect.entity.CollectRaid;
import com.jcca.web.collect.service.CollectRaidService;
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
@Component("raidVLogSaveFilterHandler")
public class RaidVLogSaveFilterHandler extends IFilterHandler<RaidCommonLogEntity> {

    @Resource
    private CollectRaidService raidService;

    @Override
    public boolean handler(RaidCommonLogEntity info) {

        CollectRaid collectRaid = new CollectRaid();
        collectRaid.setDiskType(5);
        collectRaid.setLogInfo(info.getLog());
        collectRaid.setId(MyIdUtil.getId());
        collectRaid.setAssetId(info.getAssetId());
        if (info.getCollectTime() != null) {
            collectRaid.setCollectTime(new Date(info.getCollectTime()));
        } else {
            collectRaid.setCollectCode(info.getCollectCode());
        }
        raidService.save(collectRaid);
        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
