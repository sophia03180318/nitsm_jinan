package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.vo.InspectRecordListVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author HanHW
 * @description 巡检详情
 * @className InspectDetailMapper
 * @date 2024/1/19 11:33
 * @since 2.1.0.0
 */
public interface InspectDetailMapper extends BaseMapper<InspectDetail> {

    @Select("SELECT INSPECT_CODE, INSPECT_TIME createTime, RESULT_PATH FROM INSPECT_DETAIL WHERE INSPECT_STATE != 1 " +
            "GROUP BY INSPECT_CODE, INSPECT_TIME, RESULT_PATH ORDER BY INSPECT_TIME DESC")
    List<InspectRecordListVo> findRecordList();

    @Select("SELECT INSPECT_CODE, ASSET_ID, ASSET_NAME FROM INSPECT_DETAIL WHERE INSPECT_STATE != 0 AND INSPECT_CODE = #{inspectCode} " +
            "GROUP BY INSPECT_CODE, ASSET_ID, ASSET_NAME ORDER BY ASSET_ID")
    List<InspectRecordListVo> findDownList(String inspectCode);

    @Select("SELECT INSPECT_CODE, ASSET_ID, ASSET_NAME, MAX(INSPECT_STATE) AS inspectState, REMARK " +
            "FROM INSPECT_DETAIL WHERE TARGET_STATUS = 1 AND ASSET_STATUS = 1 AND INSPECT_CODE = #{inspectCode} " +
            "GROUP BY INSPECT_CODE, ASSET_ID, ASSET_NAME, REMARK ORDER BY ASSET_ID")
    List<InspectRecordListVo> findRecordDetail(String inspectCode);

    @Select("SELECT * FROM INSPECT_DETAIL WHERE INSPECT_CODE = #{inspectCode} AND ASSET_ID = #{assetId} AND TARGET_STATUS = 1 ORDER BY TARGET_ITEM")
    List<InspectDetail> findAssetDetail(String assetId, String inspectCode);

    @Delete("DELETE FROM INSPECT_DETAIL WHERE INSPECT_CODE = #{inspectCode}")
    void deleteRecord(String inspectCode);
}
