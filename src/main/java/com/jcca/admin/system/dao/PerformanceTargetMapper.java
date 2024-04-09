package com.jcca.admin.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.PerformanceTarget;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @ Author：sophia
 * @ Date：Created in 14:53 2021/8/20
 * @ Description:
 */
public interface PerformanceTargetMapper extends BaseMapper<PerformanceTarget> {
    @Update("update PERFORMANCE_TARGET set PING_TUNNEL=#{value} where ID=#{id}")
    void setPING(@Param("id") String id, @Param("value") Integer value);
}
