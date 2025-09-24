package com.jcca.web.graph.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.graph.entity.TopoEdge;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 16:44
 */
public interface TopoEdgeMapper extends BaseMapper<TopoEdge> {
    Boolean deleteTopoEdge(@Param("nodeType") String nodeType, @Param("orgId") String orgId);

    Boolean deleteNetWorkAssetTopoEdge(@Param("nodeType") String nodeType, @Param("assetId") String assetId);

    List<TopoEdge> queryNetWorkEdge(@Param("nodeType") String nodeType, @Param("assetId") String assetId);
}
