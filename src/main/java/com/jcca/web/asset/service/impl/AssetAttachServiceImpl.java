package com.jcca.web.asset.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.asset.dao.AssetAttachMapper;
import com.jcca.web.asset.entity.AssetAttach;
import com.jcca.web.asset.service.AssetAttachService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author hanwone
 * @date 2020-04-26 15:35
 **/
@Service
public class AssetAttachServiceImpl extends ServiceImpl<AssetAttachMapper, AssetAttach> implements AssetAttachService {

    @Resource
    AssetAttachMapper assetAttachMapper;

    @Override
    public AssetAttach getByAssetId(String assetId) {
        return assetAttachMapper.getByAssetId(assetId);
    }
}