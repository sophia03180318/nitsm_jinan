package com.jcca.admin.biz.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.biz.entity.MetadataTable;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * @ Author：sophia
 * @ Date：Created in 11:49 2022/9/29
 * @ Description:
 */

public interface MetadataMapper extends BaseMapper<MetadataTable> {

    List<String> getTables();

    List<String> getComments(String tableName);

    List<MetadataTable> getAllComments();

    List<MetadataTable> getCommentType(String tableName);

    String getCreatTable(String tableName);

    List<String> taskMenu();

    List<String> taskAlarm();


    List<String> task(String tableName, String columnName);

    List<String> taskSql(String sql);
}
