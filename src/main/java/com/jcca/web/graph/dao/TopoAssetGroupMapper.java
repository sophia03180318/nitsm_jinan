package com.jcca.web.graph.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.graph.entity.TopoAssetGroup;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-09-01 19:27:08
 **/
public interface TopoAssetGroupMapper extends BaseMapper<TopoAssetGroup> {

    @Delete("delete from TOPO_ASSET_GROUP where ORG_ID=#{orgId} and NODE_TYPE=#{nodeType}")
    Boolean deleteAssetGroup(@Param("orgId") String orgId, @Param("nodeType") String nodeType);


    @Delete("delete from TOPO_ASSET_GROUP where ASSET_ID=#{assetId} and NODE_TYPE=#{nodeType}")
    Boolean deleteNetWorkAssetGroup(@Param("assetId") String assetId, @Param("nodeType") String nodeType);

    @Select("select * from TOPO_ASSET_GROUP where ORG_ID=#{orgId} and NODE_TYPE=#{nodeType} order by id desc")
    List<TopoAssetGroup> queryAssetGroup(@Param("orgId") String orgId, @Param("nodeType") String nodeType);


    @Select("select * from TOPO_ASSET_GROUP where ASSET_ID=#{assetId} and NODE_TYPE=#{nodeType} order by id desc")
    List<TopoAssetGroup> queryNetWorkAssetGroup(@Param("assetId") String assetId, @Param("nodeType") String nodeType);


}
