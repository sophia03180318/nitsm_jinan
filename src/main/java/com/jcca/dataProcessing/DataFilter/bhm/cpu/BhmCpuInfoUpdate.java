package com.jcca.dataProcessing.DataFilter.bhm.cpu;

import cn.hutool.core.util.StrUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.CollectBhmCpuEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 更新资产中的CPU硬件信息
 */
@Component("bhmCpuInfoUpdate")
public class BhmCpuInfoUpdate extends IFilterHandler<List<CollectBhmCpuEntity>> {

    @Resource
    private AssetService assetService;


    @Override
    public boolean handler(List<CollectBhmCpuEntity> info) throws ResultException, Exception {
        if(Objects.isNull(info)||info.isEmpty()){
            return true;
        }
        CollectBhmCpuEntity collectBhmCpuEntity = info.get(0);
        String assetId = collectBhmCpuEntity.getAssetId();
        Asset asset = assetService.getById(assetId);
        if(Objects.isNull(asset)){
            return true;
        }

        asset.setCpuNumber(info.size());
        asset.setCpuCoreNumber(collectBhmCpuEntity.getTotalCores());
        if(Objects.nonNull(collectBhmCpuEntity.getMaxSpeedMHz())){
            asset.setCpuFrequency(collectBhmCpuEntity.getMaxSpeedMHz().toString());
        }

        StringBuilder str = new StringBuilder("");
        if(StrUtil.isNotEmpty(collectBhmCpuEntity.getManufacturer())){
            str.append("厂商：");
            str.append(collectBhmCpuEntity.getManufacturer());
        }
        if(StrUtil.isNotEmpty(collectBhmCpuEntity.getModel())){
            str.append("型号：");
            str.append(collectBhmCpuEntity.getModel());
        }
        asset.setCpuModel(str.toString());

        assetService.updateById(asset);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
