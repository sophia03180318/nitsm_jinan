package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.entity.InspectAsset;
import com.jcca.web2.vo.ItemVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author HanHW
 * @description 巡检管理服务
 * @className InspectAssetMapper
 * @date 2025/5/19 17:34
 * @since 2.1.6.0
 */
public interface InspectAssetMapper extends BaseMapper<InspectAsset> {

    List<InspectAsset> getInspectAssets(@Param("assetIds") List<String> assetIds);

    @Select("SELECT ASSET_ID AS ID, ASSET_NAME AS NAME, MAX(INSPECT_STATE) AS STATUS FROM INSPECT_ASSET WHERE JOB_ID = #{jobId} GROUP BY ASSET_ID, ASSET_NAME")
    List<ItemVo> getAllCheckedAsset(String jobId);

    @Select("SELECT ASSET_DESK, DESK_NAME, EVENT_TYPE_ID, EVENT_TYPE_NAME FROM INSPECT_ASSET WHERE JOB_ID = #{jobId} GROUP BY ASSET_DESK, DESK_NAME, EVENT_TYPE_ID, EVENT_TYPE_NAME")
    List<InspectAsset> getAllCheckedTarget(String jobId);

    @Select("SELECT ASSET_DESK AS ID, DESK_NAME AS NAME FROM INSPECT_ASSET WHERE JOB_ID = #{jobId} GROUP BY ASSET_DESK, DESK_NAME")
    List<ItemVo> getDesksByJobId(String jobId);

    @Select("SELECT * FROM INSPECT_ASSET WHERE JOB_ID = #{jobId} ORDER BY ASSET_DESK, ASSET_ID")
    List<InspectAsset> getAllByJobId(String jobId);

    @Select("SELECT EVENT_TYPE_ID AS ID, EVENT_TYPE_NAME AS NAME, MAX(INSPECT_STATE) AS STATUS FROM INSPECT_ASSET WHERE JOB_ID = #{jobId} GROUP BY EVENT_TYPE_ID, EVENT_TYPE_NAME")
    List<ItemVo> getTargetStatus(String jobId);
}
