package com.jcca.web.graph.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.graph.entity.TopoAssetMark;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-09-01 19:27:08
 **/
public interface TopoAssetMarkMapper extends BaseMapper<TopoAssetMark> {

    @Delete("delete from TOPO_ASSET_MARK where ORG_ID=#{orgId} and NODE_TYPE=#{nodeType}")
    Boolean deleteAssetMark(@Param("orgId") String orgId, @Param("nodeType") String nodeType);

    @Delete("delete from TOPO_ASSET_MARK where ASSET_ID=#{assetId} and NODE_TYPE=#{nodeType}")
    Boolean deleteNetWorkAssetMark(@Param("assetId") String assetId, @Param("nodeType") String nodeType);

    @Select("select * from TOPO_ASSET_MARK where ORG_ID=#{orgId} and NODE_TYPE=#{nodeType} order by id")
    List<TopoAssetMark> queryAssetMark(@Param("orgId") String orgId, @Param("nodeType") String nodeType);


    @Select("select * from TOPO_ASSET_MARK where ASSET_ID=#{assetId} and NODE_TYPE=#{nodeType} order by id desc")
    List<TopoAssetMark> queryNetWorkAssetMark(@Param("assetId") String assetId, @Param("nodeType") String nodeType);

}
