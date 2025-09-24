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

    Boolean deleteAssetMark(@Param("orgId") String orgId, @Param("nodeType") String nodeType);

    Boolean deleteNetWorkAssetMark(@Param("assetId") String assetId, @Param("nodeType") String nodeType);

    List<TopoAssetMark> queryAssetMark(@Param("orgId") String orgId, @Param("nodeType") String nodeType);


    List<TopoAssetMark> queryNetWorkAssetMark(@Param("assetId") String assetId, @Param("nodeType") String nodeType);

}
