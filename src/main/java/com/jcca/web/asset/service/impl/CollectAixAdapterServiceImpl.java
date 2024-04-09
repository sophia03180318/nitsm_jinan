package com.jcca.web.asset.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.asset.dao.CollectAIXAdapterMapper;
import com.jcca.web.asset.entity.CollectAIXAdapter;
import com.jcca.web.asset.service.CollectAixAdapterService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author GodWone
 * @description 小型机IO卡采集
 * @className CollectAixAdapterServiceImpl
 * @date 2023/2/7 10:31
 * @since 2.0.0.1
 */
@Service
public class CollectAixAdapterServiceImpl extends ServiceImpl<CollectAIXAdapterMapper, CollectAIXAdapter> implements CollectAixAdapterService {

    @Resource
    private CollectAIXAdapterMapper aixAdapterMapper;

    /**
     * IO卡最新信息
     *
     * @param assetId 资产ID
     * @return 信息集合
     */
    @Override
    public List<CollectAIXAdapter> latestInfo(String assetId) {
        return aixAdapterMapper.selectLatestInfo(assetId);
    }
}
