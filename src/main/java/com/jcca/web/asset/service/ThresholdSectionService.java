package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.web.asset.entity.ThresholdSection;

import java.util.List;


/**
 * 区间阈值
 *
 * @author lyp
 */
public interface ThresholdSectionService extends IService<ThresholdSection> {


    /**
     * 查询设定的区间阈值
     *
     * @param assetId
     * @param type
     * @param flag
     * @return
     */
    ThresholdSection selectSectionConf(String assetId, String type, String flag);

    /**
     * 查询设备所有的阶段阈值
     * @param assetId
     * @return
     */
    List<ThresholdSection> selectSectionConf(String assetId);

}
