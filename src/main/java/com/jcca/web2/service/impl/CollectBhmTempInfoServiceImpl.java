package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.CollectBhmTempInfoMapper;

import com.jcca.web2.entity.CollectBhmTempInfo;
import com.jcca.web2.service.CollectBhmTempInfoService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Service
public class CollectBhmTempInfoServiceImpl extends ServiceImpl<CollectBhmTempInfoMapper, CollectBhmTempInfo> implements CollectBhmTempInfoService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAssetTempInfoBatch(List<CollectBhmTempInfo> saveList) {
        if(Objects.isNull(saveList) || saveList.isEmpty()){
            return ;
        }
        CollectBhmTempInfo collectBhmInfo = saveList.get(0);

        QueryWrapper<CollectBhmTempInfo> deleteMapper = new QueryWrapper<>();
        deleteMapper.eq("ASSET_ID", collectBhmInfo.getAssetId());
        remove(deleteMapper);
        saveBatch(saveList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAssetTempInfo(CollectBhmTempInfo copy) {
        if(Objects.isNull(copy)){
            return ;
        }
        QueryWrapper<CollectBhmTempInfo> deleteMapper = new QueryWrapper<>();
        deleteMapper.eq("ASSET_ID", copy.getAssetId());
        deleteMapper.eq("MEMBER_ID", copy.getMemberId());
        remove(deleteMapper);
        save(copy);
    }
}
