package com.jcca.web.statistics.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.ai.vo.MemoryVo;
import com.jcca.web.ai.vo.SwapVo;
import com.jcca.web.asset.vo.AssetHistoryVo;
import com.jcca.web.statistics.entity.HourMemory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 一小时统计
 *
 * @author Lvyp
 */
@Mapper
public interface HourMemoryMapper extends BaseMapper<HourMemory> {

    /**
     * 查询最大的创建时间
     *
     * @return
     */
    @Select("SELECT MAX(CREATE_TIME) FROM HOUR_MEMORY")
    Date maxCreateDate();

    @Select("SELECT MEM_USED_RATE memUsedRate, SWAP_USED_RATE swapUsedRate, END_TIME collectTime FROM HOUR_MEMORY " +
            "WHERE ASSET_ID = #{assetId} AND END_TIME BETWEEN #{startDate} AND #{endDate} ORDER BY END_TIME")
    List<AssetHistoryVo> findLineByDate(String assetId, Date startDate, Date endDate);

    @Select("SELECT MEM_USED memUsedRate , END_TIME collectDate FROM HOUR_MEMORY where  ASSET_ID = #{assetId}  and END_TIME>= #{day}order by collectDate desc")
    List<MemoryVo> findDataByDay(String assetId, Date day);

    @Select("SELECT SWAP_USED_RATE swapUsedRate , END_TIME collectDate FROM HOUR_MEMORY where  ASSET_ID = #{assetId}  and END_TIME>= #{day}  order by collectDate desc")
    List<SwapVo> findSwapByDay(String assetId, Date day);
}
