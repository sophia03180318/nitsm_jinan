package com.jcca.web.collect.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.web.collect.dao.CollectClusterMapper;
import com.jcca.web.collect.entity.CollectCluster;
import com.jcca.web.collect.service.CollectClusterService;
import com.jcca.web.graph.vo.TopoVertexVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 集群信息采集
 * @author lyp
 */
@Service
public class CollectClusterServiceImpl extends ServiceImpl<CollectClusterMapper, CollectCluster> implements CollectClusterService {


    @Resource
    private CollectClusterMapper collectCluster;


    @Override
    public List<TopoVertexVo> selectNodeById(String assetId) {

        return collectCluster.selectNodeById(assetId);
    }
}
