package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectCluster;
import com.jcca.web.graph.vo.TopoVertexVo;

import java.util.List;

/**
 * 集群设备
 * @author lyp
 */
public interface CollectClusterService  extends IService<CollectCluster> {

    /**
     * 查询集群设备在TOPO上的节点
     * @param assetId
     * @return
     */
    List<TopoVertexVo> selectNodeById(String assetId);
}
