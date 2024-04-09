package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectDS;
import com.jcca.web.common.service.bean.PullAlertLogResultReq;
import com.jcca.web.common.service.bean.PullAlertLogResultResp;

import java.util.Date;
import java.util.List;

/**
 * 磁盘阵列
 *
 * @author sophia
 */
public interface CollectDsService extends IService<CollectDS> {

    /**
     * 查询指定类型的最后一次采集数据
     * 0 Controller /1 ARRAY / 2  Logical driver/ 3 driver
     */
    List<CollectDS> findByType(String assetId, int type);

    List<CollectDS> findByDrives(String assetId, int index);

    /**
     * 查询指定array下的最后一次采集的logicalDrives
     */
    List<CollectDS> findByArray(String arrayId);

    /**
     * 查询最后采集时间
     */
    Date findLastTime(String assetId);

    /**
     * 清除两小时之外的信息
     */
    Boolean removeBeforeData(Integer removeHour);

    PullAlertLogResultResp downLogFile(PullAlertLogResultReq req) throws Exception;


}
