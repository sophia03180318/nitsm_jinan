package com.jcca.web.asset.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.alarm.vo.AlarmUnconfirmVo;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.vo.CabinetVo;
import com.jcca.web.asset.vo.DetailCabinetVo;
import com.jcca.web.graph.vo.GraphStatusVo;
import com.jcca.web2.dto.CabinetBaseInfoQueryDto;
import com.jcca.web2.entity.FileRelate;
import com.jcca.web2.vo.CabinetBaseInfoVo;
import com.jcca.web2.vo.CabinetTopoDetailVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-26 15:33:53
 **/
public interface CabinetMapper extends BaseMapper<Cabinet> {


    /**
     * 根据机房ID获取机柜列表
     *
     * @param roomId
     * @return
     */
    List<CabinetVo> listByRoomId(String roomId);

    /**
     * 查询组织下的所有机柜
     *
     * @param rowIndex
     * @param orgId
     * @return
     */
    List<Cabinet> findByOrgId(Integer rowIndex, String orgId);


    /**
     * 查询组织下的所有机柜
     *
     * @param orgId
     * @return
     */
    List<CabinetTopoDetailVo> findByOrgIdV2(String orgId);
    /**
     * 查询
     *
     * @param assetId
     * @return
     */
    Cabinet findByAssetId(@Param("assetId") String assetId);

    /**
     * 获取机柜告警状态
     *
     * @param orgId
     * @return
     */
    List<GraphStatusVo> findStatusByOrgId(String orgId);

    /**
     * 获取机柜详情
     *
     * @param id
     * @return
     */
    List<DetailCabinetVo> findDetailById(String id);

    /**
     * 获取机柜内设备告警信息
     *
     * @param id
     * @return
     */
    List<AlarmUnconfirmVo> findAlarmByCabinetId(String id);

    /**
     * 查询机柜的基础信息V2
     *
     * @param query
     * @return
     * @author Lvyp
     */
    CabinetBaseInfoVo selectCabinetBaseInfoV2(CabinetBaseInfoQueryDto query);

    List<String> topoCabinetByAssetStr(String roomId, String keyword);

    /**
     * 查询组织下的所有机柜
     *
     * @param orgId
     * @return
     */
    List<Cabinet> findCabinetByOrgId(@Param("orgId") String orgId);

    List<FileRelate> findAssetInCabinet(String cabinetId);
}
