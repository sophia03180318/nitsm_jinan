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
    @Select("SELECT * FROM COLLECT_CPU b WHERE b.COLLECT_CODE=(SELECT MAX(to_number(COLLECT_CODE)) AS code FROM COLLECT_CPU WHERE ASSET_ID=#{assetId})")
    List<CollectCpu> selectRealTimeData(String assetId);

    /**
     * 汇总指定时间段内资产的CPU平均使用率
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Select("SELECT MAX(ID) AS ID,MAX(COLLECT_TIME) AS COLLECT_TIME,MAX(to_number(COLLECT_CODE)) AS COLLECT_CODE,ASSET_ID,MAX(CREATE_TIME) AS CREATE_TIME,ROUND(AVG(CPU_USED_RATE),2) AS CPU_USED_RATE FROM COLLECT_CPU WHERE COLLECT_TIME BETWEEN #{startDate} AND #{endDate} GROUP BY ASSET_ID")
    List<CollectCpu> statisticsByDate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Select("select * from COLLECT_CPU where ID = (SELECT MAX(ID) from COLLECT_CPU)")
    CollectCpu selectMaxOne();
}
