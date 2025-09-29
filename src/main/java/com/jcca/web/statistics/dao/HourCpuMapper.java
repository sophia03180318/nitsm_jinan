package com.jcca.web.statistics.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ai.vo.CpuVo;
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

    Date maxCreateDate();

    List<AssetHistoryVo> findLineByDate(String assetId, Date startDate, Date endDate);

    AvgVo findAvgByDate(String assetId, Date startDate, Date endDate);

    List<CpuVo> findDataByDay(String assetId, Date endDate);
}
