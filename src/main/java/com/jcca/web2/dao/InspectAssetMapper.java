package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.dto.xunjian.InspectTargetDetailInfo;
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

    @Select("SELECT ASSET_ID AS ID, ASSET_NAME AS NAME, MAX(INSPECT_STATE) AS STATUS FROM INSPECT_ASSET WHERE JOB_ID = #{jobId} GROUP BY ASSET_ID, ASSET_NAME ORDER BY ASSET_ID")
    List<ItemVo> getAllCheckedAsset(String jobId);

    @Select("SELECT ASSET_DESK, DESK_NAME, EVENT_TYPE_ID, EVENT_TYPE_NAME FROM INSPECT_ASSET WHERE JOB_ID = #{jobId} GROUP BY ASSET_DESK, DESK_NAME, EVENT_TYPE_ID, EVENT_TYPE_NAME ORDER BY EVENT_TYPE_ID")
    List<InspectAsset> getAllCheckedTarget(String jobId);

    @Select("SELECT ASSET_DESK AS ID, DESK_NAME AS NAME FROM INSPECT_ASSET WHERE JOB_ID = #{jobId} GROUP BY ASSET_DESK, DESK_NAME ORDER BY ASSET_DESK")
    List<ItemVo> getDesksByJobId(String jobId);

    @Select("SELECT i.*, t.EVENT_CATEGORY FROM INSPECT_ASSET i LEFT JOIN ALARM_EVENT_TYPE t ON i.EVENT_TYPE_ID = t.ID WHERE JOB_ID = #{jobId} ORDER BY i.ASSET_ID, i.ASSET_DESK")
    List<InspectAsset> getAllByJobId(String jobId);

    @Select("SELECT EVENT_TYPE_ID AS ID, EVENT_TYPE_NAME AS NAME, MAX(INSPECT_STATE) AS STATUS, COUNT(*) AS TOTAL, " +
            "SUM(CASE WHEN INSPECT_STATE = 3 THEN 1 ELSE 0 END) AS NORMAL, " +
            "SUM(CASE WHEN INSPECT_STATE = 4 THEN 1 ELSE 0 END) AS ABNORMAL " +
            "FROM INSPECT_ASSET " +
            "WHERE JOB_ID = #{jobId} GROUP BY EVENT_TYPE_ID, EVENT_TYPE_NAME ORDER BY EVENT_TYPE_ID")
    List<ItemVo> getTargetStatus(String jobId);

    @Select("SELECT INSPECT_CODE, ASSET_ID, ASSET_NAME, TARGET_ITEM, TARGET_NAME, INSPECT_STATE, THRESHOLD_VALUE, INSPECT_VALUE, RESULT_MSG " +
            "FROM INSPECT_DETAIL WHERE INSPECT_CODE = #{inspectRecordId} AND EVENT_TYPE_ID = #{eventTypeId} AND INSPECT_STATE = 4 ORDER BY ASSET_ID")
    List<InspectTargetDetailInfo> getTargetAssetInfo(String inspectRecordId, String eventTypeId);

    @Select("SELECT * FROM INSPECT_DETAIL WHERE INSPECT_CODE = #{inspectRecordId} AND ASSET_ID = #{assetId} AND INSPECT_STATE = 4 ORDER BY TARGET_ITEM")
    List<InspectTargetDetailInfo> getAssetTargetInfo(String inspectRecordId, String assetId);
}
