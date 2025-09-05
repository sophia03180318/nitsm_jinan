package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.CollectBhmPowerInfoMapper;

import com.jcca.web2.entity.CollectBhmPowerInfo;
import com.jcca.web2.service.CollectBhmPowerInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Service
public class CollectBhmPowerInfoServiceImpl extends ServiceImpl<CollectBhmPowerInfoMapper, CollectBhmPowerInfo> implements CollectBhmPowerInfoService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAssetPcieInfoBatch(List<CollectBhmPowerInfo> saveList) {
        if(Objects.isNull(saveList) || saveList.isEmpty()){
            return ;
        }
        CollectBhmPowerInfo collectBhmInfo = saveList.get(0);

        QueryWrapper<CollectBhmPowerInfo> deleteMapper = new QueryWrapper<>();
        deleteMapper.eq("ASSET_ID", collectBhmInfo.getAssetId());

        remove(deleteMapper);
        saveBatch(saveList);
    }
}
