package com.jcca.web2.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.CollectDbProcessLockInfoMapper;

import com.jcca.web2.entity.CollectDbProcessLockInfo;
import com.jcca.web2.service.CollectDbProcessLockInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Service
public class CollectDbProcessLockInfoServiceImpl extends ServiceImpl<CollectDbProcessLockInfoMapper, CollectDbProcessLockInfo> implements CollectDbProcessLockInfoService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCollectData(List<CollectDbProcessLockInfo> collectProcessLockList, String dbId) {
        if (StrUtil.isBlank(dbId)) {
            throw new IllegalArgumentException("dbId cannot be blank");
        }
        if (CollectionUtils.isEmpty(collectProcessLockList)) {
            return ;
        }
        QueryWrapper<CollectDbProcessLockInfo> delQuery = new QueryWrapper<>();
        delQuery.eq("COLLECT_DB_ID", dbId);
        remove(delQuery);

        saveBatch(collectProcessLockList);
    }
}
