package com.jcca.web.asset.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcca.admin.biz.entity.RoomReq;
import com.jcca.admin.system.entity.SysOrgReq;
import com.jcca.admin.system.entity.SysRoomAssetMsgBean;
import com.jcca.common.bean.ResultVo;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.vo.RoomVo;
import com.jcca.web.common.service.bean.ShelvesReq;

import java.util.List;

/**
 * @author hanwone
 * @date 2020-04-26 15:35:53
 **/
public interface RoomService extends IService<Room> {


    /**
     * 根据组织ID获取机房列表
     *
     * @param orgId
     * @return
     */
    List<RoomVo> listByOrgId(String orgId);

    /**
     * 根据ID删除机房
     *
     * @param id
     */
    ResultVo<Object> delById(String id);

    /**
     * 获取登录用户管理组织下面的所有机房
     *
     * @return
     */
    List<Room> getSubjectRooms();

    /**
     * 根据机柜ID查找机柜所在机房
     *
     * @param cabinetId
     * @return
     */
    Room findByCabinetId(String cabinetId);

    /**
     * 通过名字查询所有机房
     *
     * @param content
     * @return
     */
    List<Room> findAllByName(String content);

    /*
     * 同一组织层级可以同时添加多个机房
     * syt
     */
    ResultVo<Object> saveRoomsInOrg(RoomReq req);

    /**
     * 查询机房中的设备信息
     *
     * @param roomId
     * @return
     */
    List<SysRoomAssetMsgBean> queryRoomAssets(String roomId);


    /**
     * @description: 获取机房列表
     * @author: HanHW
     * @date: 2023/11/30 14:35
     * @param: [room]
     **/
    List<Room> getRoomListV2(String orgTreeId);

    /**
     * @description: 给车站新建机房机柜
     * @author: HanHW
     * @date: 2023/11/30 14:57
     * @param: [sysOrg]
     * @return: void
     */
    void createRoomAndCabinet(SysOrgReq sysOrg);

    /**
     * 同步3D机房数据
     * */
    List<ShelvesReq> pushAssetByRoom(String roomId1,String roomId2);
}
