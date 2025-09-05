package com.jcca.web2.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.CollectDatabasesInfoMapper;
import com.jcca.web2.entity.CollectDatabasesInfo;
import com.jcca.web2.service.CollectDatabasesInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;


/**
 * 采集数据库信息
 */
@Service
public class CollectDatabasesInfoServiceImpl extends ServiceImpl<CollectDatabasesInfoMapper, CollectDatabasesInfo> implements CollectDatabasesInfoService {



    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCollectData(List<CollectDatabasesInfo> collectDatabasesInfos, String assetId) {
        if (StrUtil.isBlank(assetId)) {
            throw new IllegalArgumentException("assetId cannot be blank");
        }
        if (CollectionUtils.isEmpty(collectDatabasesInfos)) {
            return ;
        }
        QueryWrapper<CollectDatabasesInfo> delQuery = new QueryWrapper<>();
        delQuery.eq("asset_id", assetId);
        remove(delQuery);

        saveBatch(collectDatabasesInfos);
    }

}
