package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.vo.InspectOrgAssetVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Set;

/**
 * @author HanHW
 * @description 巡检管理服务
 * @className InspectMapper
 * @date 2023/11/14 17:34
 * @since 2.1.0.0
 */
public interface InspectRecordMapper extends BaseMapper<InspectRecord> {

    @Select("SELECT MODE_TYPE, MODE_NAME FROM INSPECT_RECORD GROUP BY MODE_TYPE, MODE_NAME ORDER BY MODE_TYPE")
    List<InspectRecord> findGroupByModeType();

    @Select("SELECT TARGET_ITEM, TARGET_NAME, TARGET_STATUS FROM INSPECT_RECORD WHERE MODE_TYPE = #{modeType} GROUP BY TARGET_ITEM, TARGET_NAME, TARGET_STATUS")
    List<InspectRecord> findByModeType(String modeType);

    @Select("SELECT ORG_ID, ORG_NAME FROM INSPECT_RECORD WHERE MODE_TYPE = #{modeType} GROUP BY ORG_ID, ORG_NAME")
    List<InspectRecord> findByModeTypeGroupByOrgId(String modeType);

    @Select("SELECT ASSET_ID, ASSET_NAME, ORG_ID, MAX(ASSET_STATUS) assetStatus FROM INSPECT_RECORD WHERE MODE_TYPE = #{modeType} GROUP BY ASSET_ID, ASSET_NAME, ORG_ID")
    List<InspectRecord> findByModeTypeGroupByAssetId(String modeType);

    @Select("SELECT * FROM INSPECT_RECORD WHERE ASSET_ID = #{assetId} AND MODE_TYPE = #{modeType} AND TARGET_ITEM = #{targetItem}")
    InspectRecord findOneByAssetAndTarget(String assetId, String modeType, String targetItem);

    List<InspectRecord> findLastGroupByOrgId(String inspectType);

    List<InspectRecord> findLastGroupByCabinetId(String inspectType);

    List<InspectRecord> findCabinetInfo(String cabinetId, String inspectType);

    @Select("SELECT MAX(INSPECT_CODE) FROM INSPECT_RECORD")
    String findLastInspectCode();

    @Select("SELECT COUNT(*) FROM (SELECT ASSET_ID FROM INSPECT_RECORD WHERE ASSET_STATUS = 1 AND MODE_TYPE = #{modeType} GROUP BY ASSET_ID)")
    Integer countCheckedByModeType(String modeType);

    @Select("SELECT * FROM INSPECT_RECORD WHERE ASSET_ID = #{assetId} AND TARGET_ITEM = #{targetItem} AND MODE_TYPE = #{modeType}")
    InspectRecord findTargetState(String assetId, String targetItem, String modeType);

    List<InspectRecord> findAssetState(String assetId, String inspectType);

    InspectRecord findCabinetState(String cabinetId, String inspectType);

    @Select("SELECT asset_id FROM (SELECT asset_id FROM INSPECT_RECORD WHERE TARGET_STATUS = 1 AND ASSET_STATUS = 1 ORDER BY ORG_ID, CABINET_NAME, asset_id) GROUP BY asset_id")
    List<String> findAssetIdList();

    @Select("SELECT ORG_ID, ORG_NAME FROM INSPECT_RECORD GROUP BY ORG_ID, ORG_NAME ORDER BY ORG_ID")
    List<InspectOrgAssetVo> findOrgList();

    @Select("SELECT ORG_ID, ASSET_DESK, DESK_NAME FROM INSPECT_RECORD WHERE ORG_ID = #{orgId} GROUP BY ORG_ID, ASSET_DESK, DESK_NAME")
    List<InspectOrgAssetVo> findDeskListByOrgId(String orgId);

    @Select("SELECT ASSET_ID, ASSET_NAME, ORG_ID, ASSET_DESK, ASSET_STATUS FROM INSPECT_RECORD WHERE ORG_ID = #{orgId} AND ASSET_DESK = #{assetDesk} GROUP BY ASSET_ID, ASSET_NAME, ORG_ID, ASSET_DESK, ASSET_STATUS")
    List<InspectOrgAssetVo> findAssetListByOrgDesk(String orgId, Integer assetDesk);

    @Select("SELECT * FROM INSPECT_RECORD WHERE ASSET_STATUS = 1")
    List<InspectRecord> findCheckedAsset();

    @Select("SELECT * FROM INSPECT_RECORD WHERE TARGET_STATUS = 1")
    List<InspectRecord> findCheckedTarget();

    @Update("UPDATE INSPECT_RECORD SET INSPECT_TYPE = 1 WHERE ASSET_STATUS = 1")
    void updateCheckedAsset();

    @Update("UPDATE INSPECT_RECORD SET INSPECT_TYPE = 2 WHERE TARGET_STATUS = 1")
    void updateCheckedTarget();

    List<InspectOrgAssetVo> findLineStations(@Param("stationids") Set<String> stationids);

    @Select("SELECT INSPECT_TYPE FROM INSPECT_RECORD GROUP BY INSPECT_TYPE")
    String findNowInspectType();
}
