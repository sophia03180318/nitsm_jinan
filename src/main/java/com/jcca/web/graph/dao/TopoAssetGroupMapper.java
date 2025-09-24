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

    Boolean deleteAssetGroup(@Param("orgId") String orgId, @Param("nodeType") String nodeType);


    Boolean deleteNetWorkAssetGroup(@Param("assetId") String assetId, @Param("nodeType") String nodeType);

    List<TopoAssetGroup> queryAssetGroup(@Param("orgId") String orgId, @Param("nodeType") String nodeType);


    List<TopoAssetGroup> queryNetWorkAssetGroup(@Param("assetId") String assetId, @Param("nodeType") String nodeType);


}
