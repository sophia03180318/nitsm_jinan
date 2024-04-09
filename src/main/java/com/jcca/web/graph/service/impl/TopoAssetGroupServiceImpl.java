package com.jcca.web.graph.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.graph.dao.TopoAssetGroupMapper;
import com.jcca.web.graph.entity.TopoAssetGroup;
import com.jcca.web.graph.service.TopoAssetGroupService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yu_chen
 * @date 2020-09-01 19:27
 **/
@Service
public class TopoAssetGroupServiceImpl extends ServiceImpl<TopoAssetGroupMapper, TopoAssetGroup> implements TopoAssetGroupService {

    @Resource
    private TopoAssetGroupMapper topoAssetGroupMapper;

    @Override
    public Boolean deleteAssetGroup(String orgId, String nodeType) {
        return topoAssetGroupMapper.deleteAssetGroup(orgId, nodeType);
    }

    @Override
    public Boolean deleteNetWorkAssetGroup(String assetId, String nodeType) {
        return topoAssetGroupMapper.deleteNetWorkAssetGroup(assetId, nodeType);
    }

    @Override
    public List<TopoAssetGroup> queryAssetGroup(String orgId, String nodeType) {
        return topoAssetGroupMapper.queryAssetGroup(orgId, nodeType);
    }

    @Override
    public List<TopoAssetGroup> queryNetWorkAssetGroup(String assetId, String nodeType) {
        return topoAssetGroupMapper.queryNetWorkAssetGroup(assetId, nodeType);
    }
}