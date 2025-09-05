package com.jcca.web2.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.CollectDbLogSettingMapper;

import com.jcca.web2.entity.CollectDbLogSetting;
import com.jcca.web2.service.CollectDbLogSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CollectDbLogSettingServiceImpl extends ServiceImpl<CollectDbLogSettingMapper, CollectDbLogSetting> implements CollectDbLogSettingService  {



    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCollectData(List<CollectDbLogSetting> collectDbLogSettingList, String assetId) {
        if (StrUtil.isBlank(assetId)) {
            throw new IllegalArgumentException("assetId cannot be blank");
        }
        if (CollectionUtils.isEmpty(collectDbLogSettingList)) {
            return ;
        }
        QueryWrapper<CollectDbLogSetting> delQuery = new QueryWrapper<>();
        delQuery.eq("ASSET_ID", assetId);
        remove(delQuery);

        saveBatch(collectDbLogSettingList);
    }



}
