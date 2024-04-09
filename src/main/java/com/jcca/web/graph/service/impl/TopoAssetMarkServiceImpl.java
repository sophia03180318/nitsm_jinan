package com.jcca.web.graph.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.graph.dao.TopoAssetMarkMapper;
import com.jcca.web.graph.entity.TopoAssetMark;
import com.jcca.web.graph.service.TopoAssetMarkService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yu_chen
 * @date 2020-09-01 19:27
 **/
@Service
public class TopoAssetMarkServiceImpl extends ServiceImpl<TopoAssetMarkMapper, TopoAssetMark> implements TopoAssetMarkService {

    @Resource
    private TopoAssetMarkMapper topoAssetMarkMapper;

    @Override
    public Boolean deleteAssetMark(String orgId, String nodeType) {
        return topoAssetMarkMapper.deleteAssetMark(orgId, nodeType);
    }

    @Override
    public Boolean deleteNetWorkAssetMark(String assetId, String nodeType) {
        return topoAssetMarkMapper.deleteNetWorkAssetMark(assetId, nodeType);
    }

    @Override
    public List<TopoAssetMark> queryAssetMark(String orgId, String nodeType) {
        return topoAssetMarkMapper.queryAssetMark(orgId, nodeType);
    }

    @Override
    public List<TopoAssetMark> queryNetWorkAssetMark(String assetId, String nodeType) {
        return topoAssetMarkMapper.queryNetWorkAssetMark(assetId, nodeType);
    }
}