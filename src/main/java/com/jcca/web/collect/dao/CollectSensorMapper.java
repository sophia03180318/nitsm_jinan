package com.jcca.web.collect.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.collect.entity.CollectSensor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 感应器采集数据信息
 *
 * @author Lvyp
 */
@Mapper
public interface CollectSensorMapper extends BaseMapper<CollectSensor> {


    /**
     * 查询信息
     *
     * @param assetId
     * @return
     */
    List<CollectSensor> selectRealTimeData(@Param("assetId") String assetId);

    /**
     * 查询设备内最大的温度
     *
     * @return
     */
    List<CollectSensor> selectMaxValue();
}
