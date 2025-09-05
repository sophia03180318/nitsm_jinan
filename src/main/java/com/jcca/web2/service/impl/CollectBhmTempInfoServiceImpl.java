package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.CollectBhmTempInfoMapper;

import com.jcca.web2.entity.CollectBhmTempInfo;
import com.jcca.web2.service.CollectBhmTempInfoService;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


@Service
public class CollectBhmTempInfoServiceImpl extends ServiceImpl<CollectBhmTempInfoMapper, CollectBhmTempInfo> implements CollectBhmTempInfoService {


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
}
