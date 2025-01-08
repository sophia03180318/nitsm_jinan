package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.SysActionLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author hanwone
 * @date 2020-04-06 12:11:47
 **/
public interface SysActionLogService extends IService<SysActionLog> {

    /**
     * 删除所有日志信息
     *
     * @return
     */
    Boolean removeAll();

    /**
     * 采集数据转存为文件
     *
     * @param table      表名
     * @param dateFormat 日期
     * @return 查出的数据
     */
    List<Map<String, Object>> getData(String table, Date dateFormat);

    /**
     * 删除指定天数之前的数据
     *
     * @param table      表名
     * @param dateFormat 日期
     */
    void removeData(String table, Date dateFormat);

    /**
     * 删除最旧的5000条数据
     *
     * @param tables      要删除数据的表
     * @param amount      要保留的数据天数
     * @param recoverPath 存储数据路径
     */
    void remove5000(String[] tables, int amount, String recoverPath);

    /**
     * 获取最新一条日志数据
     *
     * @return 最新一条数据
     */
    SysActionLog getLatestOne();

    void recoverData(String tableName, BufferedReader reader) throws IOException;

    void resetdb();
}
