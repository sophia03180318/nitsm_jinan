package com.jcca.web2.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.CollectDbLockInfoMapper;

import com.jcca.web2.entity.CollectDbLockInfo;
import com.jcca.web2.service.CollectDbLockInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Service
public class CollectDbLockInfoServiceImpl extends ServiceImpl<CollectDbLockInfoMapper, CollectDbLockInfo> implements CollectDbLockInfoService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCollectData(List<CollectDbLockInfo> collectDbLockList, String dbId) {
        if (StrUtil.isBlank(dbId)) {
            throw new IllegalArgumentException("dbId cannot be blank");
        }
        if (CollectionUtils.isEmpty(collectDbLockList)) {
            return ;
        }
        QueryWrapper<CollectDbLockInfo> delQuery = new QueryWrapper<>();
        delQuery.eq("COLLECT_DB_ID", dbId);
        remove(delQuery);

        saveBatch(collectDbLockList);

    }
}
