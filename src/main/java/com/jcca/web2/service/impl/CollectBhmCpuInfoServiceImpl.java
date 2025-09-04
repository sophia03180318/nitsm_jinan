package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.CollectBhmCpuInfoMapper;
import com.jcca.web2.entity.CollectBhmCpuInfo;
import com.jcca.web2.service.CollectBhmCpuInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;


/**
 * 管理口cpu信息
 */
@Service
public class CollectBhmCpuInfoServiceImpl extends ServiceImpl<CollectBhmCpuInfoMapper, CollectBhmCpuInfo> implements CollectBhmCpuInfoService  {

    @Resource
    private CollectBhmCpuInfoMapper cpuInfoMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAssetCpuInfoBatch(List<CollectBhmCpuInfo> infoList) {
        if(Objects.isNull(infoList) || infoList.isEmpty()){
            return ;
        }
        CollectBhmCpuInfo collectBhmCpuInfo = infoList.get(0);

        QueryWrapper<CollectBhmCpuInfo> deleteMapper = new QueryWrapper<>();
        deleteMapper.eq("ASSET_ID", collectBhmCpuInfo.getAssetId());

        remove(deleteMapper);
        saveBatch(infoList);
    }
}
