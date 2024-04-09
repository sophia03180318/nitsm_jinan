package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.controller.route.bean.AssetLinkAssetVo;
import com.jcca.web.collect.entity.CollectRoute;
import com.jcca.web.collect.service.bean.TopoRouteVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 采集路由信息
 *
 * @author lyp
 */
@Mapper
public interface CollectRouteMapper extends BaseMapper<CollectRoute> {
    @Delete(value = "delete from COLLECT_ROUTE")
    Boolean deleteAll();

    //查询含有对端设备IP的信息
    @Select(value = "select * from COLLECT_ROUTE where at_net_address is not null")
    List<CollectRoute> getExistTarget();

    List<TopoRouteVo> getRouteMsg(String assetId);

    List<AssetLinkAssetVo> findAtAssetAndPort(String assetId);
}
