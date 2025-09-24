package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectSystemTime;
import com.jcca.web.common.controller.bean.DurationResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统时间相关信息采集
 *
 * @author Lvyp
 */
@Mapper
public interface CollectSystemTimeMapper extends BaseMapper<CollectSystemTime> {

    /**
     * 查询系统时间相关信息
     *
     * @param assetId
     * @return
     */
    List<CollectSystemTime> selectAssetNewSystem(@Param("assetId") String assetId);

    /**
     * 获取设备运行时长
     */
    List<Long> getRunTime(String assetId);


    List<DurationResp> listGroupByAsset();


    List<DurationResp> pageGroupByAsset(@Param("pageSize") Integer pageSize, @Param("pageIndex") Integer pageIndex);
}
