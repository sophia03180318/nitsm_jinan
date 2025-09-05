package com.jcca.web2.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.CollectDbSlowSqlMapper;

import com.jcca.web2.entity.CollectDbSlowSql;
import com.jcca.web2.service.CollectDbSlowSqlService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CollectDbSlowSqlServiceImpl extends ServiceImpl<CollectDbSlowSqlMapper,CollectDbSlowSql> implements CollectDbSlowSqlService{


    @Override
    public void updateCollectData(List<CollectDbSlowSql> collectSlowList, String assetId) {
        if (StrUtil.isBlank(assetId)) {
            throw new IllegalArgumentException("assetId cannot be blank");
        }
        if (CollectionUtils.isEmpty(collectSlowList)) {
            return ;
        }
        QueryWrapper<CollectDbSlowSql> delQuery = new QueryWrapper<>();
        delQuery.eq("ASSET_ID", assetId);
        remove(delQuery);

        saveBatch(collectSlowList);
    }
}
