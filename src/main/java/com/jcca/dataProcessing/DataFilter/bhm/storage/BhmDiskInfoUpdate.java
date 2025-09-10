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
public class BhmDiskInfoUpdate extends IFilterHandler<CollectBhmStorageEntity> {

    @Resource
    private AssetService assetService;

    @Override
    public boolean handler(CollectBhmStorageEntity collectBhmStorageEntity) throws ResultException, Exception {
        if(Objects.isNull(collectBhmStorageEntity)){
            return true;
        }
        String assetId = collectBhmStorageEntity.getAssetId();
        Asset asset = assetService.getById(assetId);

        asset.setDiskTotal(collectBhmStorageEntity.getCount());
        assetService.updateById(asset);
        return true;
    }


    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }

}
