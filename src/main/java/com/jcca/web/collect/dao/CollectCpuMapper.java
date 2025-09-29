package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectCpu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * CPU采集数据
 *
 * @author Lvyp
 */
@Mapper
public interface CollectCpuMapper extends BaseMapper<CollectCpu> {

    /**
     * 获取实时数据
     *
     * @param assetId
     * @return
     */
    List<CollectCpu> selectRealTimeData(String assetId);

    /**
     * 汇总指定时间段内资产的CPU平均使用率
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<CollectCpu> statisticsByDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    CollectCpu selectMaxOne();
}
