package com.jcca.admin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.system.entity.PerformanceTarget;

import java.util.List;

/**
 * @ Author：sophia
 * @ Date：Created in 9:58 2021/8/12
 * @ Description:
 */
public interface PerformanceTargetService extends IService<PerformanceTarget> {

    void setPIng(String id);

    /**
     * @description: 通过资产ID获取资产采集指标
     * @author: HanHW
     * @date: 2023/11/16 14:17
     * @param: [assetId]
     * @return: java.util.List<com.jcca.admin.system.entity.PerformanceTarget>
     **/
    List<PerformanceTarget> getListByAssetIdV2(String assetId);

}
