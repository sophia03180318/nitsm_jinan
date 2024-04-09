package com.jcca.web.asset.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.AssetImportTask;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @ Author：sophia
 * @ Date：Created in 11:25 2021/7/8
 * @ Description:
 */

public interface AssetImportTaskMapper extends BaseMapper<AssetImportTask> {
    @Select("select id from ASSET_TASK where CREATE_TIME=(select max(CREATE_TIME) from ASSET_TASK)")
    String getLastOneId();

    @Select("UPDATE ASSET_TASK SET STATUS=#{status} where CREATE_TIME=(select max(CREATE_TIME) from ASSET_TASK)")
    void setLastStatus(@Param("status") int status);

    @Select("select STATUS FROM ASSET_TASK where ID=#{id}")
    int getStatusById(@Param("id") String id);
}
