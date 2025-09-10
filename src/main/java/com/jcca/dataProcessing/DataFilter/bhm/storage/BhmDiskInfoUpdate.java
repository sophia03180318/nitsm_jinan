package com.jcca.dataProcessing.DataFilter.bhm.storage;


import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.CollectBhmStorageEntity;
import com.jcca.dataProcessing.Entity.ReadFishDiskEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Component("bhmDiskInfoUpdate")
public class BhmDiskInfoUpdate extends IFilterHandler<List<CollectBhmStorageEntity>> {

    @Resource
    private AssetService assetService;

    @Override
    public boolean handler(List<CollectBhmStorageEntity> info) throws ResultException, Exception {
        if(Objects.isNull(info)||info.isEmpty()){
            return true;
        }
        String assetId = info.get(0).getAssetId();
        Asset asset = assetService.getById(assetId);

        int diskCount=0;
        for (CollectBhmStorageEntity collectBhmStorageEntity : info) {
            List<ReadFishDiskEntity> diskInfos = collectBhmStorageEntity.getDiskInfos();
            if(Objects.isNull(diskInfos) || diskInfos.isEmpty()){
                continue;
            }
            diskCount =diskCount+ diskInfos.size();
        }

        asset.setDiskTotal(diskCount);
        assetService.updateById(asset);
        return true;
    }


    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
