package com.jcca.web.asset.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcca.admin.biz.entity.RoomReq;
import com.jcca.admin.system.entity.SysOrg;
import com.jcca.admin.system.entity.SysOrgReq;
import com.jcca.admin.system.entity.SysRoomAssetMsgBean;
import com.jcca.admin.system.service.SysOrgService;
import com.jcca.common.bean.ResultVo;
import com.jcca.common.bean.constant.OrgTypeConst;
import com.jcca.common.enums.ResultEnum;
import com.jcca.common.shiro.util.ShiroUtil;
import com.jcca.common.utils.MyIdUtil;
import com.jcca.common.utils.ResultVoUtil;
import com.jcca.common.utils.ToolUtil;
import com.jcca.web.asset.dao.RoomMapper;
import com.jcca.web.asset.entity.Cabinet;
import com.jcca.web.asset.entity.Room;
import com.jcca.web.asset.service.CabinetService;
import com.jcca.web.asset.service.RoomService;
import com.jcca.web.asset.vo.RoomVo;
import com.jcca.web.common.service.bean.ShelvesReq;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hanwone
 * @date 2020-04-26 15:35
 **/
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    @Resource
    private RoomMapper roomMapper;
    @Resource
    private CabinetService cabinetService;
    @Resource
    private SysOrgService orgService;

    /**
     * 根据组织ID获取机房列表
     *
     * @param orgId
     * @return
     */
    @Override
    public List<RoomVo> listByOrgId(String orgId) {
        return roomMapper.listByOrgId(orgId);
    }

    /**
     * 根据ID删除机房
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Object> delById(String id) {
        // 查看机房内是否有机柜
        QueryWrapper<Cabinet> query = Wrappers.query();
        query.eq("room_id", id);
        List<Cabinet> cabinets = cabinetService.list(query);
        if (CollectionUtil.isNotEmpty(cabinets)) {
            return ResultVoUtil.error("请先删除机房内机柜");
        }

        // 删除机房
        roomMapper.deleteById(id);

        return ResultVoUtil.success("成功");
    }

    /**
     * 获取登录用户管理组织下面的所有机房
     *
     * @return
     */
    @Override
    public List<Room> getSubjectRooms() {
        QueryWrapper<Room> room = Wrappers.query();
        room.in("org_id", ShiroUtil.getSubjectOrgIds());

        return this.list(room);
    }

    /**
     * 根据机柜ID查找机柜所在机房
     *
     * @param cabinetId
     * @return
     */
    @Override
    public Room findByCabinetId(String cabinetId) {
        return roomMapper.findByCabinetId(cabinetId);
    }

    /**
     * gen'ju
     */
    @Override
    public List<Room> findAllByName(String content) {
        QueryWrapper<Room> queryWrapper = new QueryWrapper<Room>();
        queryWrapper.eq("NAME", content);
        return roomMapper.selectList(queryWrapper);
    }

    /*
     * 同一组织层级下可以同时添加多个机房
     * syt
     */
    @Override
    public ResultVo<Object> saveRoomsInOrg(RoomReq req) {
        if (Objects.isNull(req)) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "参数不可为空");
        }


        ArrayList<Room> rooms = new ArrayList<>();
        // 中英文兼容并进行分隔
        List<String> split = Arrays.asList(ToolUtil.cToe(req.getName()).split(","));
        // 若元素为空(两个连续的逗号分割完后会出现空元素,例:"机房1,,机房2,机房3"),将其移除,避免空字符bug
        List<String> afterTreatment = split.stream().filter(StrUtil::isNotBlank).collect(Collectors.toList());

        // 同一级组织下机房名称重复的不能添加
        List<String> existName = roomMapper.getRoomNames(req.getOrgId());
        // 编辑时只编辑一条
        if (StrUtil.isEmpty(req.getId())) {
            boolean contains = CollectionUtil.containsAny(existName, afterTreatment);
            if (contains) {
                return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "所添加的机房名称在该组织下有重复");
            }
            for (int i = 0; i < afterTreatment.size(); i++) {
                Room room = new Room();
                room.setId(MyIdUtil.getId());
                room.setOrgId(req.getOrgId());
                room.setName(afterTreatment.get(i));
                room.setRemark(req.getRemark());
                rooms.add(room);
            }
            // 添加机房
            boolean b = this.saveBatch(rooms);
            return ResultVoUtil.success("添加成功");
        }

       /* boolean contains = CollectionUtil.contains(existName, req.getName());
        if (contains) {
            return ResultVoUtil.error(ResultEnum.PARAM_ERROR.getCode(), "所添加的机房名称在该组织下有重复");
        }*/

        Room room = new Room();
        room.setId(req.getId());
        room.setOrgId(req.getOrgId());
        room.setName(req.getName());
        room.setRemark(req.getRemark());
        this.updateById(room);

        return ResultVoUtil.success("编辑成功");

//        return ResultVoUtil.error();
    }


    @Override
    public List<SysRoomAssetMsgBean> queryRoomAssets(String roomId) {
        return roomMapper.selectRoomAssets(roomId);
    }

    /**
     * @description: 获取机房列表
     * @author: HanHW
     * @date: 2023/11/30 14:32
     * @param: [room]
     */
    @Override
    public List<Room> getRoomListV2(Room room) {
        String orgTreeId = room.getOrgTreeId();

        List<String> orgIds;
        QueryWrapper<Room> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(room.getName())) {
            wrapper.like("name", room.getName());
        }

        orgIds = ShiroUtil.getSubjectOrgIds();
        if (!StringUtils.isEmpty(orgTreeId)) {
            SysOrg org = orgService.getById(orgTreeId);
            Integer type = org.getType();
            if (type == OrgTypeConst.LINE) {
                orgIds = orgService.getIdByline(orgTreeId);
            }
            if (type == OrgTypeConst.CENTER || type == OrgTypeConst.STATION) {
                orgIds = Collections.singletonList(orgTreeId);
            }
        }
        if (StringUtils.isEmpty(orgIds)) {
            return new ArrayList<>();
        }
        wrapper.in("org_id", orgIds);
        wrapper.orderByDesc("modify_time");
        List<Room> records = this.list(wrapper);
        for (Room record : records) {
            record.setOrgName(orgService.getById(record.getOrgId()).getTitle());
        }

        return records;
    }

    /**
     * @description: 给车站新建机房机柜
     * @author: HanHW
     * @date: 2023/11/30 14:57
     * @param: [req]
     * @return: void
     */
    @Override
    public void createRoomAndCabinet(SysOrgReq req) {

        String orgId = req.getId();
        String orgTitle = req.getTitle();
        Room room = new Room();
        room.setId(MyIdUtil.getId());
        room.setOrgId(orgId);
        room.setName(orgTitle + "机房");
        this.save(room);

        if (OrgTypeConst.STATION == req.getType()) {
            this.createStationCabinet(room.getId());
        }
//        if (OrgTypeConst.CENTER == req.getType()) {
//            req.setId(room.getId());
//            this.createCenterCabinet(req);
//        }

    }

    private void createCenterCabinet(SysOrgReq req) {
        Integer rowSize = req.getRowSize();
        Integer amount = req.getAmount();
        String title = req.getTitle();
        String fix = (title.contains("客专") || title.contains("高铁")) ? "K" : "P";
        List<Cabinet> cabinets = new ArrayList<>();
        for (int i = 0; i < rowSize; i++) {
            for (int j = 0; j < amount; j++) {
                Cabinet cabinet = new Cabinet();
                cabinet.setId(MyIdUtil.getId());
                cabinet.setName(fix + i + fillName(j + ""));
                cabinet.setCode(Integer.parseInt(i + fillName(j + "")));
                cabinet.setRoomId(req.getId());
                cabinet.setRowIndex(i);
                cabinet.setColumnIndex(j);
                cabinets.add(cabinet);
            }
        }
        cabinetService.saveBatch(cabinets);
    }

    private String fillName(String j) {
        if (j.length() < 2) {
            j = "0" + j;
        }
        return j;
    }

    private void createStationCabinet(String roomId) {
        Cabinet cabinet = new Cabinet();
        cabinet.setId(MyIdUtil.getId());
        cabinet.setName("工控机柜");
        cabinet.setCode(1);
        cabinet.setRoomId(roomId);
        cabinet.setRowIndex(1);
        cabinet.setColumnIndex(1);
        cabinetService.save(cabinet);

        cabinet.setId(MyIdUtil.getId());
        cabinet.setName("采集机柜");
        cabinet.setCode(2);
        cabinet.setColumnIndex(2);
        cabinetService.save(cabinet);
    }

    @Override
    public List<ShelvesReq> pushAssetByRoom(String roomId1,String roomId2) {
        List<ShelvesReq> assets = roomMapper.pushAssetByRoom(roomId1,roomId2);
        return assets;
    }

}