package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.CollectBhmPcieInfoMapper;

import com.jcca.web2.entity.CollectBhmPcieInfo;
import com.jcca.web2.service.CollectBhmPcieInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Service
public class CollectBhmPcieInfoServiceImpl extends ServiceImpl<CollectBhmPcieInfoMapper, CollectBhmPcieInfo> implements CollectBhmPcieInfoService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAssetPcieInfoBatch(List<CollectBhmPcieInfo> saveList) {
        if(Objects.isNull(saveList) || saveList.isEmpty()){
            return ;
        }
        CollectBhmPcieInfo collectBhmInfo = saveList.get(0);

        QueryWrapper<CollectBhmPcieInfo> deleteMapper = new QueryWrapper<>();
        deleteMapper.eq("ASSET_ID", collectBhmInfo.getAssetId());

        remove(deleteMapper);
        saveBatch(saveList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAssetPcieInfo(CollectBhmPcieInfo copy) {
        if(Objects.isNull(copy)){
            return ;
        }
        QueryWrapper<CollectBhmPcieInfo> deleteMapper = new QueryWrapper<>();
        deleteMapper.eq("ASSET_ID", copy.getAssetId());
        deleteMapper.eq("PCIE_ID", copy.getPcieId());

        remove(deleteMapper);
        save(copy);


    }
}
