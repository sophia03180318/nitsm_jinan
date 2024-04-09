package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectCluster;
import com.jcca.web.graph.vo.TopoVertexVo;

import java.util.List;

/**
 * 采集集群信息
 * @author lyp
 */
public interface CollectClusterMapper  extends BaseMapper<CollectCluster> {



    /**
     * 查询集群在TOPO上的节点
     * @param assetId  资产ID
     * @return
     */
    List<TopoVertexVo> selectNodeById(String assetId);

}
