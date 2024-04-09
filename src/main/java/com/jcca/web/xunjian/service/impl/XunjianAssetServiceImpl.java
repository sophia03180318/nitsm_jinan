package com.jcca.web.xunjian.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.xunjian.dao.XunjianAssetDao;
import com.jcca.web.xunjian.entity.XunjianAsset;
import com.jcca.web.xunjian.service.XunjianAssetService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yu_chen
 * @date 2021-02-24 16:09
 **/
@Service
public class XunjianAssetServiceImpl extends ServiceImpl<XunjianAssetDao, XunjianAsset> implements XunjianAssetService {

    @Override
    public void removeByAssetId(String assetId) {
        QueryWrapper<XunjianAsset> queryWrapper = new QueryWrapper<XunjianAsset>();
        queryWrapper.eq("ASSET_ID", assetId);
        List<XunjianAsset> list = list(queryWrapper);
        for (XunjianAsset xunjianAsset : list) {
            xunjianAsset.setDeleteFlag(0);
        }
        if (!list.isEmpty()) {
            updateBatchById(list);
        }
    }


}