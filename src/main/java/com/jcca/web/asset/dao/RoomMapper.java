package com.jcca.web.asset.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcca.admin.system.entity.SysRoomAssetMsgBean;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.vo.RoomVo;
import com.jcca.web.common.service.bean.ShelvesReq;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-26 15:35:53
 **/
public interface RoomMapper extends BaseMapper<Room> {


    /**
     * 根据组织ID获取机房列表
     *
     * @param orgId
     * @return
     */
    @Select(value = "select * from room where org_id = #{orgId}")
    List<RoomVo> listByOrgId(String orgId);

    /**
     * 查询机房信息
     *
     * @param assetId
     * @return
     */
    @Select(value = "select * from room r where r.id=(select c.room_id from asset_attach c where c.asset_id=#{assetId})")
    RoomVo selectByAssetId(@Param("assetId") String assetId);

    /**
     * 根据机柜ID查找机柜所在机房
     *
     * @param cabinetId
     * @return
     */
    @Select(value = "select * from room where id = (select room_id from cabinet where id = #{cabinetId})")
    Room findByCabinetId(@Param("cabinetId") String cabinetId);

    /**
     * 查找所有机柜名称
     *
     * @return: java.util.List<java.lang.String>
     * @Author: syt
     * @Date: 2021/8/18/018 11:58
     */
    @Select(value = "select name from room where ORG_ID = #{orgId}")
    List<String> getRoomNames(@Param("orgId") String orgId);

    /**
     * 查询机房内的设备信息
     *
     * @param roomId
     * @return
     */
    List<SysRoomAssetMsgBean> selectRoomAssets(String roomId);

    @Select("select aa.*,c.name as cabinetName from (select a.id, a.name,a.ip,a.ip2, a.DESK as categoryId, a.ASSET_IMAGE AS threeModel,i.ROOM_ID as areaId,i.CABINET_ID AS cabinetId,i.START_POSITION AS startU from asset a join  ASSET_ATTACH  i on a.id=i.asset_id where IS_DEL=1 and  (ROOM_ID=#{roomId1} or ROOM_ID=#{roomId2} )and CABINET_ID is not null) aa join CABINET c on aa.cabinetId=c.id")
    List<ShelvesReq> pushAssetByRoom(String roomId1, String roomId2);
}
