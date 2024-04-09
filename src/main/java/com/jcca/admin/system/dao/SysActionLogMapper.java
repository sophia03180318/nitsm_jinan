package com.jcca.admin.system.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.SysActionLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author hanwone
 * @date 2020-04-06 12:11:47
 **/
public interface SysActionLogMapper extends BaseMapper<SysActionLog> {


    /**
     * 删除所有日志信息
     *
     * @return
     */
    @Delete(value = "delete from sys_action_log")
    Boolean deleteAll();

    /**
     * 采集数据转存为文件
     *
     * @param tableStr   表名
     * @param dateFormat 日期
     * @return 查出的数据
     */
    @Select("SELECT * FROM ${tableStr} WHERE ROWNUM < 5000 AND CREATE_TIME < #{dateFormat} ORDER BY CREATE_TIME")
    List<Map<String, Object>> getData(@Param("tableStr") String tableStr, @Param("dateFormat") Date dateFormat);

    /**
     * 删除指定日期的数据
     *
     * @param tableStr   表名
     * @param dateFormat 日期
     */
    @Delete("DELETE FROM ${tableStr} WHERE ROWNUM < 5000 AND CREATE_TIME < #{dateFormat}")
    void removeData(@Param("tableStr") String tableStr, @Param("dateFormat") Date dateFormat);

    @Select("SELECT * FROM SYS_ACTION_LOG WHERE ROWNUM = 1 ORDER BY ID DESC")
    SysActionLog getLatestOne();
}
