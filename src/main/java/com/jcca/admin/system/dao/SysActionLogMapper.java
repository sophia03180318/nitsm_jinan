package com.jcca.admin.system.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.SysActionLog;
import org.apache.ibatis.annotations.Param;

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
    Boolean deleteAll();

    /**
     * 采集数据转存为文件
     *
     * @param tableStr   表名
     * @param dateFormat 日期
     * @return 查出的数据
     */
    List<Map<String, Object>> getData(@Param("tableStr") String tableStr, @Param("dateFormat") Date dateFormat);

    /**
     * 删除指定日期的数据
     *
     * @param tableStr   表名
     * @param dateFormat 日期
     */
    void removeData(@Param("tableStr") String tableStr, @Param("dateFormat") Date dateFormat);

    SysActionLog getLatestOne();

    void removeData(@Param("tableStr") String tableStr);
}
