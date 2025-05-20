package com.jcca.web2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web2.dao.InspectAssetMapper;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.service.InspectAssetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author HanHW
 * @description 巡检管理服务
 * @className InspectAssetServiceImpl
 * @date 2025/5/19 17:32
 * @since 2.1.6.0
 */
@Service
public class InspectAssetServiceImpl extends ServiceImpl<InspectAssetMapper, InspectAsset> implements InspectAssetService {

    @Resource
    private InspectAssetMapper inspectAssetMapper;

    @Override
    public List<InspectAsset> getInspectAssets(List<String> assetIds) {
        return inspectAssetMapper.getInspectAssets(assetIds);
    }

    @Override
    public void removeByJobId(String jobId) {
        QueryWrapper<InspectAsset> query1 = Wrappers.query();
        query1.eq("JOB_ID", jobId);
        this.remove(query1);
    }
}
