package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.entity.MaintenancePlan;
import com.jcca.web2.vo.MaintenanceStatisticsVo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @description:
 * @author: sophia
 * @create: 2023/11/16 16:34
 **/
public interface MaintenancePlanMapper extends BaseMapper<MaintenancePlan> {

    @Select("select type, occur_month month, count(type) count from MAINTENANCE_PLAN where occur_year = #{year} group by occur_month, type order by occur_month")
    List<MaintenanceStatisticsVo> statistics(int year);

    @Select("select type from MAINTENANCE_PLAN where occur_year = #{year} group by type")
    List<String> getTypeList(int year);
}