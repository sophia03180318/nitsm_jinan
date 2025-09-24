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
    Boolean deleteTopoPoints(@Param("nodeType") String nodeType, @Param("orgId") String orgId);


    Boolean deleteNetWorkAssetTopoPoints(@Param("nodeType") String nodeType, @Param("assetId") String assetId);

    List<TopoPoints> queryNetWorkPoints(@Param("nodeType") String nodeType, @Param("assetId") String assetId);
}
