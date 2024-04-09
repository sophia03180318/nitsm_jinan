package com.jcca.web.collect.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.collect.entity.CollectConnect;

import java.util.List;


/**
 * <p>
 * 服务类
 * </p>
 *
 * @author LuBan
 * @since 2021-04-27
 */
public interface CollectConnectService extends IService<CollectConnect> {

    /**
     * 更新实时缓存
     */
    void updateRealTimeData(List<CollectConnect> connectList);

    /**
     * 查询数据
     *
     * @param assetId
     * @return
     */
    List<CollectConnect> getRealTimeData(String assetId);

    /**
     * 获取告警的标识
     *
     * @return
     */
    String getAlarmCode(List<CollectConnect> connectList);

    /**
     * 更新
     */
    void updateBatchByAssetId(List<CollectConnect> entityList);


}
