package com.jcca.web2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web2.entity.InspectRecord;
import com.jcca.web2.vo.InspectOrgAssetVo;
import com.jcca.web2.vo.InspectShareVo;
import org.apache.ibatis.annotations.Delete;
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

    List<InspectRecord> findGroupByModeType();

    List<InspectRecord> findByModeType(String modeType);

    List<InspectRecord> findByModeTypeGroupByOrgId(String modeType);

    List<InspectRecord> findByModeTypeGroupByAssetId(String modeType);

    List<InspectRecord> findOneByAssetAndTarget(String assetId, String modeType, String targetItem);

    List<InspectRecord> findLastGroupByOrgId(String inspectType);

    List<InspectRecord> findLastGroupByCabinetId(String inspectType);

    List<InspectRecord> findCabinetInfo(String cabinetId, String inspectType);

    String findLastInspectCode();

    Integer countCheckedByModeType(String modeType);

    InspectRecord findTargetState(String assetId, String targetItem, String modeType);

    List<InspectRecord> findAssetState(String assetId, String inspectType);

    InspectRecord findCabinetState(String cabinetId, String inspectType);

    List<String> findAssetIdList();

//    List<InspectOrgAssetVo> findOrgList(@Param("orgIds") List<String> orgIds);

    List<InspectOrgAssetVo> findOrgList();

    List<InspectOrgAssetVo> findDeskListByOrgId(String orgId);

    List<InspectOrgAssetVo> findAssetListByOrgDesk(String orgId, Integer assetDesk);

    List<InspectRecord> findCheckedAsset();

    List<InspectRecord> findCheckedTarget();

    void updateCheckedAsset();

    void updateCheckedTarget();

    List<InspectOrgAssetVo> findLineStations(@Param("stationids") Set<String> stationids);

    String findNowInspectType();

    void deleteByOrgId(String orgId);

    List<InspectShareVo> findRecordByJobId(String jobId);

    List<InspectRecord> findByJobId(String jobId);
}
