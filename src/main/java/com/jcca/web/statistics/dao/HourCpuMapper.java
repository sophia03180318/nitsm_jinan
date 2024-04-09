package com.jcca.web.statistics.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.vo.AssetHistoryVo;
import com.jcca.web.asset.vo.AvgVo;
import com.jcca.web.statistics.entity.HourCpu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * cpu数据一小时汇总
 *
 * @author Lvyp
 */
@Mapper
public interface HourCpuMapper extends BaseMapper<HourCpu> {

    @Select("SELECT MAX(CREATE_TIME) FROM HOUR_CPU")
    Date maxCreateDate();

    @Select("SELECT CPU_USED_RATE cpuUsedRate, END_TIME collectTime FROM HOUR_CPU " +
            "WHERE ASSET_ID = #{assetId} AND END_TIME BETWEEN #{startDate} AND #{endDate} ORDER BY END_TIME")
    List<AssetHistoryVo> findLineByDate(String assetId, Date startDate, Date endDate);

    @Select("SELECT ROUND(AVG(m.MEM_USED_RATE), 2) avgMem, ROUND(AVG(m.SWAP_USED_RATE), 2) avgSwap, ROUND(AVG(c.CPU_USED_RATE), 2) avgCpu " +
            "FROM HOUR_MEMORY m " +
            "LEFT JOIN HOUR_CPU c ON m.ASSET_ID = c.ASSET_ID " +
            "WHERE m.ASSET_ID = #{assetId} AND m.END_TIME BETWEEN #{startDate} AND #{endDate}")
    AvgVo findAvgByDate(String assetId, Date startDate, Date endDate);
}
