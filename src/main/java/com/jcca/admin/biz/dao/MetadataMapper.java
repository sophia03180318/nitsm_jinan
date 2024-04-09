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

    @Select("select table_name from user_tables order by table_name")
    List<String> getTables();


    @Select("select COLUMN_NAME from user_col_comments where table_name =#{tableName}")
    List<String> getComments(String tableName);

    @Select("select TABLE_NAME AS tName ,COLUMN_NAME AS cName from user_col_comments where table_name not like 'BIN%'")
    List<MetadataTable> getAllComments();

    @Select("select column_name as cname, data_type as ctype, data_length as clenght from user_tab_columns where table_name = upper(#{tableName})")
    List<MetadataTable> getCommentType(String tableName);

    @Select("select dbms_metadata.get_ddl('TABLE',#{tableName}) from dual")
    String getCreatTable(String tableName);


    List<String> taskMenu();

    List<String> taskAlarm();


    List<String> task(String tableName, String columnName);

    List<String> taskSql(String sql);
}
