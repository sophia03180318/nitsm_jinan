package com.jcca.dataProcessing.DataFilter.bhm.power;


import cn.hutool.core.util.StrUtil;
import com.jcca.common.exception.ResultException;
import com.jcca.dataProcessing.Entity.CollectBhmPowerEntity;
import com.jcca.dataProcessing.support.IFilterHandler;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 电源信息安装
 */
@Component("bhmPowerInfoUpdate")
public class BhmPowerInfoUpdate  extends IFilterHandler<CollectBhmPowerEntity> {

    @Resource
    private AssetService assetService;


    @Override
    public boolean handler(CollectBhmPowerEntity info) throws ResultException, Exception {
        if(Objects.isNull(info)){
            return true;
        }
        String assetId = info.getAssetId();
        Asset asset = assetService.getById(assetId);

        if(Objects.isNull(asset)){
            return true;
        }
        StringBuilder str = new StringBuilder("");
        if(StrUtil.isNotEmpty(info.getName())){
            str.append(info.getName());
        }
        if(StrUtil.isNotEmpty(info.getManufacturer())){
            str.append(info.getManufacturer());
        }
        if(StrUtil.isNotEmpty(info.getModel())){
            str.append(info.getModel());
        }
        String modelStr = str.toString();
        String powerModel = asset.getPowerModel();
        if(StrUtil.isNotEmpty(powerModel)){
            asset.setPowerModel(modelStr);
        }else if(!powerModel.contains(modelStr)){
            asset.setPowerModel(powerModel+"|"+modelStr);
        }
        asset.setPowerTotal(info.getCount());

        assetService.updateById(asset);

        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
