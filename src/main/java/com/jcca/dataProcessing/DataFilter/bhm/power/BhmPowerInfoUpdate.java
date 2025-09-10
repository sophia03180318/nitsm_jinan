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
public class BhmPowerInfoUpdate  extends IFilterHandler<List<CollectBhmPowerEntity>> {

    @Resource
    private AssetService assetService;


    @Override
    public boolean handler(List<CollectBhmPowerEntity> info) throws ResultException, Exception {
        if(Objects.isNull(info)|| info.isEmpty()){
            return true;
        }
        String assetId = info.get(0).getAssetId();
        Asset asset = assetService.getById(assetId);

        if(Objects.isNull(asset)){
            return true;
        }

        StringBuilder str = new StringBuilder("");
        for (CollectBhmPowerEntity collectBhmPowerEntity : info) {
            if(StrUtil.isNotEmpty(collectBhmPowerEntity.getName())){
                str.append(collectBhmPowerEntity.getName());
            }
            if(StrUtil.isNotEmpty(collectBhmPowerEntity.getManufacturer())){
                str.append(collectBhmPowerEntity.getManufacturer());
            }
            if(StrUtil.isNotEmpty(collectBhmPowerEntity.getModel())){
                str.append(collectBhmPowerEntity.getModel());
            }
        }
        asset.setPowerModel(str.toString());
        asset.setPowerTotal(info.size());



        return true;
    }

    @Override
    public boolean isNeedNextHandle(Boolean flag) {
        return flag;
    }
}
