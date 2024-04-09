package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectRaid;

import java.util.Date;
import java.util.List;

/**
 * 磁盘阵列
 *
 * @author sophia
 */
public interface CollectRaidService extends IService<CollectRaid> {

    /**
     * 查询最近一次采集信息
     * 类型 0:池 1:Mdisk 2:卷组 3capacity 4Drive 5log
     */
    List<CollectRaid> findByType(String assetId, String groupId, int type);


    /**
     * 查询DriveList
     */
    List<CollectRaid> findDrive(String assetId);

    /**
     * 查询最后采集时间
     */
    Date findLastTime(String assetId);

    /**
     * 清除两小时之外的信息
     */
    Boolean removeBeforeData(Integer removeHour);
}
