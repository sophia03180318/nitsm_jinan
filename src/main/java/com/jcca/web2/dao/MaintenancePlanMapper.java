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

    List<MaintenanceStatisticsVo> statistics(int year);

    List<String> getTypeList(int year);
}