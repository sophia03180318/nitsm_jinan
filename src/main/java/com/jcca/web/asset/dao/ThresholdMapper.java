package com.jcca.web.asset.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.ThresholdAsset;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-05-20 10:28:42
 **/
public interface ThresholdMapper extends BaseMapper<ThresholdAsset> {

    @Select("select RUNNINGTIME_DEVIATION from THRESHOLD_ASSET where RUNNINGTIME_DEVIATION is not null and asset_mode = #{assetMode} group by RUNNINGTIME_DEVIATION")
    List<Integer> findRuntimeByAssetMode(Integer assetMode);

}
