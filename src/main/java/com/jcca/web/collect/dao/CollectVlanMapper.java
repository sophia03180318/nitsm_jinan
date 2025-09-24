package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectVlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 采集的vlan数据
 *
 * @author Lvyp
 */
@Mapper
public interface CollectVlanMapper extends BaseMapper<CollectVlan> {


    /**
     * 获取实时数据
     *
     * @param assetId
     * @return
     */
    List<CollectVlan> selectRealTimeData(String assetId);

}
