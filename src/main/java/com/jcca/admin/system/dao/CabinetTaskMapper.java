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
    @Select("select id from CABINET_TASK where CREATE_TIME=(select max(CREATE_TIME) from CABINET_TASK)")
    String getLastOneId();

    @Select("UPDATE CABINET_TASK SET STATUS=#{status} where CREATE_TIME=(select max(CREATE_TIME) from CABINET_TASK)")
    void setLastStatus(@Param("status") int status);

    @Select("select STATUS FROM CABINET_TASK where ID=#{id}")
    int getStatusById(@Param("id") String id);
}
