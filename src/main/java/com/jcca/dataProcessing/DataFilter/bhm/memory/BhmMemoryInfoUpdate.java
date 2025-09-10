package com.jcca.dataProcessing.DataFilter.bhm.memory;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.CollectBhmMemoryEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 资产内内存更新
 */
@Component("bhmMemoryInfoUpdate")
public class BhmMemoryInfoUpdate  extends IFilterHandler<List<CollectBhmMemoryEntity>> {

    @Resource
    private AssetService assetService;


    @Override
    public boolean handler(List<CollectBhmMemoryEntity> info) throws ResultException, Exception {
        if(Objects.isNull(info)||info.isEmpty()){
            return true;
        }
        CollectBhmMemoryEntity collectBhmMemoryEntity = info.get(0);
        Asset asset = assetService.getById(collectBhmMemoryEntity.getAssetId());

        if(Objects.isNull(asset)){
            return true;
        }

        StringBuilder builder = new StringBuilder("");
        for (CollectBhmMemoryEntity bhmMemoryEntity : info) {
            builder.append(bhmMemoryEntity.getName());
            if(Objects.nonNull(bhmMemoryEntity.getCapacityMiB())){
                builder.append("容量：");
                builder.append(bhmMemoryEntity.getCapacityMiB());
                builder.append("M");
            }
            if(StrUtil.isNotEmpty(bhmMemoryEntity.getManufacturer())){
                builder.append("厂商：");
                builder.append(bhmMemoryEntity.getManufacturer());
            }
        }

        asset.setMemory(builder.toString());
        assetService.updateById(asset);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
