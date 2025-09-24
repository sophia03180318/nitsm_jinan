package com.jcca.admin.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.CabinetTask;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @ Author：sophia
 * @ Date：Created in 9:57 2021/8/12
 * @ Description:
 */
public interface CabinetTaskMapper extends BaseMapper<CabinetTask> {
    String getLastOneId();

    void setLastStatus(@Param("status") int status);

    int getStatusById(@Param("id") String id);
}
