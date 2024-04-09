package com.jcca.web.graph.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.graph.entity.TopoPoints;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author zhaozheng@jccatech.com
 * @date 2020/7/1 16:45
 */
public interface TopoPointsMapper extends BaseMapper<TopoPoints> {
    @Delete("delete from TOPO_POINTS where Edge_Type=#{nodeType} and ORG_ID=#{orgId}")
    Boolean deleteTopoPoints(@Param("nodeType") String nodeType, @Param("orgId") String orgId);


    @Delete("delete from TOPO_POINTS where Edge_Type=#{nodeType} and ASSET_ID=#{assetId}")
    Boolean deleteNetWorkAssetTopoPoints(@Param("nodeType") String nodeType, @Param("assetId") String assetId);

    @Select("select * from TOPO_POINTS where EDGE_TYPE=#{nodeType} and ASSET_ID=#{assetId}")
    List<TopoPoints> queryNetWorkPoints(@Param("nodeType") String nodeType, @Param("assetId") String assetId);
}
