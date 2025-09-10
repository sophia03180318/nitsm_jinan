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
public class BhmCpuInfoUpdate extends IFilterHandler<CollectBhmCpuEntity> {

    @Resource
    private AssetService assetService;


    @Override
    public boolean handler(CollectBhmCpuEntity info) throws ResultException, Exception {
        if(Objects.isNull(info)){
            return true;
        }
        String assetId = info.getAssetId();
        Asset asset = assetService.getById(assetId);
        if(Objects.isNull(asset)){
            return true;
        }

        asset.setCpuNumber(info.getCount());
        asset.setCpuCoreNumber(info.getTotalCores());
        if(Objects.nonNull(info.getMaxSpeedMHz())){
            asset.setCpuFrequency(info.getMaxSpeedMHz().toString());
        }

        StringBuilder str = new StringBuilder("");
        if(StrUtil.isNotEmpty(info.getManufacturer())){
            str.append("厂商：");
            str.append(info.getManufacturer());
        }
        if(StrUtil.isNotEmpty(info.getModel())){
            str.append("型号：");
            str.append(info.getModel());
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
