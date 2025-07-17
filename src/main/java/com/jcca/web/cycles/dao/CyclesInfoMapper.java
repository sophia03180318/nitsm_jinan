package com.jcca.web.cycles.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.cycles.entity.CyclesInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: 周期信息
 * @author: Lvyp
 * @create: 2024/11/20 09:56
 */
public interface CyclesInfoMapper extends BaseMapper<CyclesInfo> {


    /**
     * 查询
     *
     * @param name
     * @return
     */
    List<CyclesInfo> queryAll(@Param("name") String name);

    /**
     * 查询此范围内是否存在天窗
     *
     * @param orgId
     * @param week
     * @param time
     * @return
     */
    List<CyclesInfo> selectByOrgIdAndTime(@Param("orgId") String orgId, @Param("week") int week, @Param("time") Integer time);

    /**
     * 通过ID查询
     *
     * @param id
     * @return
     */
    CyclesInfo selectInfoById(@Param("id") String id);
}
