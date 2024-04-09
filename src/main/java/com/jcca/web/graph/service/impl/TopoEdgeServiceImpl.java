package com.jcca.web.graph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.graph.dao.TopoEdgeMapper;
import com.jcca.web.graph.entity.TopoEdge;
import com.jcca.web.graph.service.TopoEdgeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 16:35
 */
@Service
public class TopoEdgeServiceImpl extends ServiceImpl<TopoEdgeMapper, TopoEdge> implements TopoEdgeService {
    @Resource
    private TopoEdgeMapper topoEdgeMapper;

    @Override
    public List<TopoEdge> getTopoEdge(String nodeType, String orgId) {
        QueryWrapper<TopoEdge> query = Wrappers.query();
        query.eq("EDGE_TYPE", nodeType);
        query.eq("ORG_ID", orgId);

        return this.list(query);
    }

    @Override
    public List<TopoEdge> queryNetWorkEdge(String nodeType, String assetId) {
        return topoEdgeMapper.queryNetWorkEdge(nodeType, assetId);
    }

    @Override
    public Boolean deleteTopoEdge(String nodeType, String orgId) {
        return topoEdgeMapper.deleteTopoEdge(nodeType, orgId);
    }

    @Override
    public Boolean deleteNetWorkAssetTopoEdge(String nodeType, String assetId) {
        return topoEdgeMapper.deleteNetWorkAssetTopoEdge(nodeType, assetId);
    }

    @Override
    public Boolean saveTopoEdge(List<TopoEdge> list) {
        return this.saveBatch(list);
    }
}
