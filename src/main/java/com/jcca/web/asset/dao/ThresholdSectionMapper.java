package com.jcca.web.asset.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.ThresholdSection;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 区间阈值
 *
 * @author lyp
 */
public interface ThresholdSectionMapper extends BaseMapper<ThresholdSection> {

    /**
     * 查询区间阈值配置
     *
     * @param assetId
     * @param type
     * @param flag
     * @return
     */
    ThresholdSection selectSectionConf(@Param("assetId") String assetId, @Param("type") String type, @Param("flag") String flag);

    /**
     * 上下限
     * @param assetId
     * @return
     */
    List<ThresholdSection> selectSectionConfList(@Param("assetId") String assetId);

}
