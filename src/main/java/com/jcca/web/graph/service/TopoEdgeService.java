package com.jcca.web.graph.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.graph.entity.TopoEdge;

import java.util.List;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 16:33
 */
public interface TopoEdgeService extends IService<TopoEdge> {
    /**
     * 获取连线
     *
     * @param nodeType 节点的类型
     * @param orgId    组织结构
     * @return
     */
    List<TopoEdge> getTopoEdge(String nodeType, String orgId);

    /**
     * 查询网络设备对端设备
     *
     * @param nodeType
     * @param assetId
     * @return
     */
    List<TopoEdge> queryNetWorkEdge(String nodeType, String assetId);

    /**
     * 删除连线
     *
     * @param nodeType
     * @param orgId
     * @return
     */
    Boolean deleteTopoEdge(String nodeType, String orgId);

    Boolean deleteNetWorkAssetTopoEdge(String nodeType, String assetId);

    /**
     * 保存连线
     *
     * @param list
     * @return
     */
    Boolean saveTopoEdge(List<TopoEdge> list);
}
