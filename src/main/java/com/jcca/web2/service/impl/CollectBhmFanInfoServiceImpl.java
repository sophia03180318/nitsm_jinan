package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.CollectBhmFanInfoMapper;
import com.jcca.web2.entity.CollectBhmCpuInfo;
import com.jcca.web2.entity.CollectBhmFanInfo;
import com.jcca.web2.service.CollectBhmFanInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


/**
 * 处理管理口风扇
 */
@Service
public class CollectBhmFanInfoServiceImpl extends ServiceImpl<CollectBhmFanInfoMapper, CollectBhmFanInfo> implements CollectBhmFanInfoService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAssetFanInfoBatch(List<CollectBhmFanInfo> infoList) {
        if(Objects.isNull(infoList) || infoList.isEmpty()){
            return ;
        }
        CollectBhmFanInfo collectBhmFanInfo = infoList.get(0);

        QueryWrapper<CollectBhmFanInfo> deleteMapper = new QueryWrapper<>();
        deleteMapper.eq("ASSET_ID", collectBhmFanInfo.getAssetId());

        remove(deleteMapper);
        saveBatch(infoList);
    }
}
