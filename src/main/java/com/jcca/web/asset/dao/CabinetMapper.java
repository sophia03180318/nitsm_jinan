package com.jcca.web.asset.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.web.alarm.vo.AlarmUnconfirmVo;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.vo.CabinetVo;
import com.jcca.web.asset.vo.DetailCabinetVo;
import com.jcca.web.graph.vo.GraphStatusVo;
import com.jcca.web2.dto.CabinetBaseInfoQueryDto;
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
    @Select(value = "select * from cabinet where room_id = #{roomId} order by code asc")
    List<CabinetVo> listByRoomId(String roomId);

    /**
     * 查询组织下的所有机柜
     *
     * @param rowIndex
     * @param orgId
     * @return
     */
    @Select(value = "select c.* from cabinet c left join room r on c.room_id = r.id where r.org_id = #{orgId} order by c.row_index,c.column_index")
    List<Cabinet> findByOrgId(Integer rowIndex, String orgId);


    /**
     * 查询组织下的所有机柜
     *
     * @param orgId
     * @return
     */
    @Select(value = "select c.*,r.name as roomName from cabinet c left join room r on c.room_id = r.id where r.org_id = #{orgId} order by c.column_index")
    List<CabinetTopoDetailVo> findByOrgIdV2(String orgId);
    /**
     * 查询
     *
     * @param assetId
     * @return
     */
    @Select("select c.* from cabinet c where c.id=(select t.CABINET_ID from ASSET_ATTACH t where t.ASSET_ID=#{assetId})")
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

    @Select("select DISTINCT(CABINET_ID) from ASSET_ATTACH c join (select ID from asset where name like CONCAT(CONCAT('%', #{keyword}) , '%') or ip = #{keyword}) a on c.ASSET_ID=a.id where ROOM_ID =#{roomId}")
    List<String> topoCabinetByAssetStr(String roomId, String keyword);
}
