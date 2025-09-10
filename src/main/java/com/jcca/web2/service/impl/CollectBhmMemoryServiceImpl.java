package com.jcca.web2.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.CollectBhmMemoryMapper;

import com.jcca.web2.entity.CollectBhmMemoryInfo;
import com.jcca.web2.service.CollectBhmMemoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Objects;


@Service
public class CollectBhmMemoryServiceImpl extends ServiceImpl<CollectBhmMemoryMapper, CollectBhmMemoryInfo> implements CollectBhmMemoryService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAssetFanInfoBatch(List<CollectBhmMemoryInfo> infoList) {
        if(Objects.isNull(infoList) || infoList.isEmpty()){
            return ;
        }
        CollectBhmMemoryInfo collectBhmInfo = infoList.get(0);

        QueryWrapper<CollectBhmMemoryInfo> deleteMapper = new QueryWrapper<>();
        deleteMapper.eq("ASSET_ID", collectBhmInfo.getAssetId());

        remove(deleteMapper);
        saveBatch(infoList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAssetFanInfo(CollectBhmMemoryInfo copy) {
        if(Objects.isNull(copy)){
            return ;
        }

        QueryWrapper<CollectBhmMemoryInfo> deleteMapper = new QueryWrapper<>();
        deleteMapper.eq("ASSET_ID", copy.getAssetId());
        deleteMapper.eq("MEMORY_ID", copy.getMemoryId());

        remove(deleteMapper);
        save(copy);

    }
}
