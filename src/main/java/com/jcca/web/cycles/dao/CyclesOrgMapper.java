package com.jcca.web.cycles.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.cycles.entity.CyclesOrg;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @description: 周期信息
 * @author: Lvyp
 * @create: 2024/11/20 09:56
 */
public interface CyclesOrgMapper extends BaseMapper<CyclesOrg> {

    /**
     * 批量保存
     *
     * @param orgList
     */
    void saveBatch(@Param("orgList") List<CyclesOrg> orgList);
}
