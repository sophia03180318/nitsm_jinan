package com.jcca.web.cycles.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.cycles.entity.CyclesTimes;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: 周期信息
 * @author: Lvyp
 * @create: 2024/11/20 09:56
 */
public interface CyclesTimesMapper extends BaseMapper<CyclesTimes> {

    /**
     * 批量保存
     *
     * @param timesList
     */
    void saveBatch(@Param("timesList") List<CyclesTimes> timesList);
}
