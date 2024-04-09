package com.jcca.web.graph.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.graph.dao.TopoPointsMapper;
import com.jcca.web.graph.entity.TopoPoints;
import com.jcca.web.graph.service.TopoPointsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 16:35
 */
@Service
public class TopoPointsServiceImpl extends ServiceImpl<TopoPointsMapper, TopoPoints> implements TopoPointsService {
    @Resource
    private TopoPointsMapper topoPointsMapper;

    @Override
    public List<TopoPoints> getTopoPoints(String nodeType, String orgId) {
        QueryWrapper<TopoPoints> query = Wrappers.query();
        query.eq("EDGE_TYPE", nodeType);
        query.eq("ORG_ID", orgId);

        return this.list(query);
    }

    @Override
    public List<TopoPoints> queryNetWorkPoints(String nodeType, String assetId) {
        return topoPointsMapper.queryNetWorkPoints(nodeType, assetId);
    }

    @Override
    public Boolean deleteTopoPoints(String nodeType, String orgId) {
        return topoPointsMapper.deleteTopoPoints(nodeType, orgId);
    }

    @Override
    public Boolean deleteNetWorkAssetTopoPoints(String nodeStyle, String assetId) {
        return topoPointsMapper.deleteNetWorkAssetTopoPoints(nodeStyle, assetId);
    }

    @Override
    public Boolean saveTopoPoints(List<TopoPoints> list) {
        return this.saveBatch(list);
    }
}
