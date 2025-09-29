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
    List<RoomVo> listByOrgId(String orgId);

    /**
     * 查询机房信息
     *
     * @param assetId
     * @return
     */
    RoomVo selectByAssetId(@Param("assetId") String assetId);

    /**
     * 根据机柜ID查找机柜所在机房
     *
     * @param cabinetId
     * @return
     */
    Room findByCabinetId(@Param("cabinetId") String cabinetId);

    /**
     * 查找所有机柜名称
     *
     * @return: java.util.List<java.lang.String>
     * @Author: syt
     * @Date: 2021/8/18/018 11:58
     */
    List<String> getRoomNames(@Param("orgId") String orgId);

    /**
     * 查询机房内的设备信息
     *
     * @param roomId
     * @return
     */
    List<SysRoomAssetMsgBean> selectRoomAssets(String roomId);

    List<ShelvesReq> pushAssetByRoom(String roomId1, String roomId2);
}
