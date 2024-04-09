package com.jcca.web.asset.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.asset.entity.ThresholdProcess;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 进程阈值
 *
 * @author Lvyp
 */
@Mapper
public interface ThresholdProcessMapper extends BaseMapper<ThresholdProcess> {


    void updateGroupHostMode(String assetIds, String processName, Integer mode);

    void updateByAssetIdAndProcessName(@Param("processName") String processName, @Param("assetId") String assetId, @Param("collectStatus") Integer collectStatus,@Param("processId")String processId);

    void updateHostModeByAssetCode(@Param("assetCode") String assetCode, @Param("hostMode") Integer hostMode);

}
