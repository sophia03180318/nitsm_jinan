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
    String getLastOneId();

    void setLastStatus(@Param("status") int status);

    int getStatusById(@Param("id") String id);
}
