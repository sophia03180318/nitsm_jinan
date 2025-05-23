package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.dto.InspectAssetDetailInfo;
import com.jcca.web2.dto.InspectTargetDetailInfo;
import com.jcca.web2.entity.InspectDetail;
import com.jcca.web2.vo.InspectRecordListVo;
import com.jcca.web2.vo.ItemVo;
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

    @Select("select asset_id from inspect_detail where inspect_code = #{inspectCode} group by asset_id")
    List<String> totalAsset(String inspectCode);

    @Select("select asset_desk as id, desk_name as name from INSPECT_DETAIL where INSPECT_CODE = #{inspectCode} group by asset_desk, desk_name order by asset_desk")
    List<ItemVo> deskList(String inspectCode);

    @Select("select count(*) from (select asset_id, asset_desk from inspect_detail where inspect_code = #{inspectCode} and asset_desk = #{assetDesk} group by asset_id, asset_desk)")
    Integer totalDesk(String inspectCode, Integer assetDesk);

    @Select("select count(*) from (select asset_id from inspect_detail where inspect_code = #{inspectCode} and asset_desk = #{assetDesk} and inspect_state = #{inspectState} group by asset_id)")
    Integer stateDesk(String inspectCode, Integer assetDesk, int inspectState);

    @Select("select count(*) from (select asset_id from inspect_detail  where inspect_code = #{inspectCode} and inspect_state = #{inspectState} group by asset_id)")
    Integer abnormalAsset(String inspectCode, int inspectState);

    @Select("select inspect_code, asset_id, asset_name, asset_ip1, org_name, room_name, cabinet_name, max(inspect_state) inspectState " +
            "from inspect_detail where inspect_code = #{inspectCode} and asset_desk in ${desks} " +
            "group by inspect_code, asset_id, asset_name, asset_ip1, org_name, room_name, cabinet_name order by asset_id")
    List<InspectAssetDetailInfo> getAssetDetail(String inspectCode, String desks);

    @Select("SELECT INSPECT_CODE, ASSET_ID, TARGET_ITEM, TARGET_NAME, INSPECT_STATE, THRESHOLD_VALUE, INSPECT_VALUE, RESULT_MSG, REMARK " +
            "FROM INSPECT_DETAIL WHERE INSPECT_CODE = #{inspectCode} AND ASSET_ID = #{assetId} ORDER BY ASSET_ID")
    List<InspectTargetDetailInfo> getTargetDetail(String inspectCode, String assetId);
}
